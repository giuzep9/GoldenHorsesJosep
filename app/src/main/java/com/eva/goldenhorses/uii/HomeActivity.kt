package com.eva.goldenhorses.uii

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.eva.goldenhorses.MusicService
import com.eva.goldenhorses.R
import com.eva.goldenhorses.SessionManager
//import com.eva.goldenhorses.data.AppDatabase
//import com.eva.goldenhorses.data.JugadorDAO
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.repository.JugadorRepository
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.eva.goldenhorses.viewmodel.JugadorViewModel
import com.eva.goldenhorses.viewmodel.JugadorViewModelFactory
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Location
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.eva.goldenhorses.utils.obtenerPaisDesdeUbicacion
import androidx.compose.ui.res.stringResource
import com.eva.goldenhorses.ui.RankingActivity
import com.eva.goldenhorses.utils.aplicarIdioma
import com.eva.goldenhorses.utils.obtenerIdioma
import java.util.Locale

class HomeActivity : ComponentActivity() {

    private lateinit var jugadorViewModel: JugadorViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup para barra de estado
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(0, 0, 0, 0)
            insets
        }

        // Música
        startService(Intent(this, MusicService::class.java))

        // ViewModel y Repository para Firebase
        val repository = JugadorRepository() // Usamos el repositorio real que conecta con Firebase
        val factory = JugadorViewModelFactory(repository)
        jugadorViewModel = factory.create(JugadorViewModel::class.java)

        // Obtener el nombre del jugador desde el Intent
        val nombreJugadorIntent = intent.getStringExtra("jugador_nombre")

        if (!nombreJugadorIntent.isNullOrEmpty()) {
            SessionManager.guardarJugador(this, nombreJugadorIntent)
        }

        val nombreJugador = SessionManager.obtenerJugador(this)
        Log.d("HomeActivity", "Nombre obtenido de SessionManager: $nombreJugador")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        obtenerUbicacion(nombreJugador)

        setContent {
            HomeScreenWithTopBar(this, jugadorViewModel, nombreJugador)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val context = aplicarIdioma(newBase) // Usa tu función LanguageUtils
        super.attachBaseContext(context)
    }

    private fun obtenerUbicacion(nombreJugador: String) {
        // Solicitar permiso si no está concedido
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        // Obtener última ubicación conocida
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val lat = it.latitude
                    val lon = it.longitude
                    Log.d("UBICACION", "Lat: $lat, Lon: $lon")

                    // Guardar ubicación en Firebase
                    jugadorViewModel.actualizarUbicacion(nombreJugador, lat, lon)
                    jugadorViewModel.actualizarPaisDesdeUbicacion(this, lat, lon)

                    // Mostramos país con Toast
                    val pais = obtenerPaisDesdeUbicacion(this, lat, lon)
                    Toast.makeText(this, "Estás en: $pais", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Función para obtener el país a partir de la latitud y longitud
    private fun obtenerPaisDesdeUbicacion(context: Context, lat: Double, lon: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        return addresses?.firstOrNull()?.countryName ?: "Desconocido"
    }

    // Seleccionar canción
    private val selectMusicLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("custom_music_uri", it.toString()).apply()

            // Reiniciar servicio con la nueva música
            val musicIntent = Intent(this, MusicService::class.java).apply {
                action = MusicService.ACTION_CHANGE_MUSIC
                putExtra("MUSIC_URI", it.toString())
            }
            startService(musicIntent)
        }
    }

    fun abrirSelectorMusica() {
        selectMusicLauncher.launch(arrayOf("audio/*"))
    }
}

@Composable
fun HomeScreenWithTopBar(
    context: Context,
    viewModel: JugadorViewModel,
    nombreJugador: String
) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var isMusicMutedState by remember { mutableStateOf(false) }
    val jugador by viewModel.jugador.collectAsState()

    // Calcular el país desde la latitud y longitud del jugador
    val pais = remember(jugador) {
        val currentJugador = jugador
        val lat = currentJugador?.latitud
        val lon = currentJugador?.longitud

        if (lat != null && lon != null) {
            obtenerPaisDesdeUbicacion(context, lat, lon)
        } else null
    }

    LaunchedEffect(nombreJugador) {
        viewModel.iniciarSesion(nombreJugador)
        isMusicMutedState = sharedPreferences.getBoolean("isMusicMuted", false)
    }

    GoldenHorsesTheme {
        Scaffold(
            topBar = {
                AppTopBar(
                    context = context,
                    isMusicMuted = isMusicMutedState,
                    onToggleMusic = { newState ->
                        isMusicMutedState = newState
                        sharedPreferences.edit().putBoolean("isMusicMuted", newState).apply()
                    },
                    jugador = jugador, // Pasamos el jugador cargado
                    pais = pais, // Pasamos el país a la AppTopBar
                    onChangeMusicClick = {
                        (context as? HomeActivity)?.abrirSelectorMusica()
                    }

                )
            }
        ) { paddingValues ->
            HomeScreen(
                jugador = jugador,
                onPlayClick = {
                    context.startActivity(Intent(context, PlayerSelectionActivity::class.java).apply {
                        putExtra("jugador_nombre", nombreJugador)
                    })
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )

        }
    }

}

@Composable
fun HomeScreen(
    jugador: Jugador?,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val partidas = jugador?.partidas ?: 0
    val victorias = jugador?.victorias ?: 0
    val nombreJugador = jugador?.nombre ?: "Cargando..."
    val context = LocalContext.current
    val idioma = obtenerIdioma(context)
    val botonJugarImage = if (idioma == "en") R.drawable.boton_play else R.drawable.boton_jugar

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_home),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.user_icon),
                contentDescription = "User Icon",
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )

            Text(
                text = nombreJugador,
                fontSize = 50.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(10.dp)
            )

            Text(
                text = "${stringResource(R.string.numero_partidas)}: $partidas",
                fontSize = 18.sp,
                color = Color.Black
            )
            Text(
                text = "${stringResource(R.string.victorias)}: $victorias",
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 32.dp)
            )


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_premio), // Asegúrate de tener este icono en res/drawable
                    contentDescription = "Icono Ranking",
                    modifier = Modifier
                        .size(120.dp)
                        .clickable {
                            context.startActivity(Intent(context, RankingActivity::class.java))
                        }
                        .padding(bottom = 16.dp)
                )
                Image(
                    painter = painterResource(id = botonJugarImage),
                    contentDescription = "Botón JUGAR / START",
                    modifier = Modifier
                        .size(200.dp, 80.dp)
                        .clickable { onPlayClick() }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHomeScreenWithTopBar() {
    val fakeJugador = Jugador(
        nombre = "JugadorDemo",
        monedas = 100,
        partidas = 10,
        victorias = 4,
        palo = "Copas"
    ).apply {
        latitud = 43.2630  // Bilbao
        longitud = -2.9350
    }

    val paisSimulado = "Spain"
    var isMusicMuted by remember { mutableStateOf(false) }

    GoldenHorsesTheme {
        Scaffold(
            topBar = {
                AppTopBar(
                    context = LocalContext.current,
                    isMusicMuted = isMusicMuted,
                    onToggleMusic = { isMusicMuted = it },
                    jugador = fakeJugador,
                    pais = paisSimulado
                )
            }
        ) { padding ->
            HomeScreen(
                jugador = fakeJugador,
                onPlayClick = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}




