package com.eva.goldenhorses.uii

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.eva.goldenhorses.MusicService
import com.eva.goldenhorses.R
import com.eva.goldenhorses.SessionManager
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.repository.JugadorRepository
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.eva.goldenhorses.utils.aplicarIdioma
import com.eva.goldenhorses.utils.obtenerIdioma
import com.eva.goldenhorses.utils.obtenerPaisDesdeUbicacion
import com.eva.goldenhorses.viewmodel.JugadorViewModel
import com.eva.goldenhorses.viewmodel.JugadorViewModelFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow

class PlayerSelectionActivity : ComponentActivity() {

    private lateinit var jugadorViewModel: JugadorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(0, 0, 0, 0)
            insets
        }

        jugadorViewModel = JugadorViewModel(JugadorRepository())
        val nombreJugador = intent.getStringExtra("jugador_nombre") ?: ""

        setContent {
            PlayerSelectionScreenWithTopBar(context = this, viewModel = jugadorViewModel, nombreJugador = nombreJugador)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val context = aplicarIdioma(newBase)
        super.attachBaseContext(context)
    }

    private val selectMusicLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("custom_music_uri", it.toString()).apply()
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

@SuppressLint("CommitPrefEdits")
@Composable
fun PlayerSelectionScreenWithTopBar(
    context: Context,
    viewModel: JugadorViewModel,
    nombreJugador: String
) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var isMusicMutedState by remember { mutableStateOf(sharedPreferences.getBoolean("isMusicMuted", false)) }
    val jugador by viewModel.jugador.collectAsState()
    val pais = remember(jugador) {
        val lat = jugador?.latitud
        val lon = jugador?.longitud
        if (lat != null && lon != null) obtenerPaisDesdeUbicacion(context, lat, lon) else null
    }

    Scaffold(
        topBar = {
            AppTopBar(
                context = context,
                isMusicMuted = isMusicMutedState,
                onToggleMusic = { newState ->
                    isMusicMutedState = newState
                    sharedPreferences.edit().putBoolean("isMusicMuted", newState).apply()
                },
                jugador = jugador,
                pais = pais,
                onChangeMusicClick = {
                    (context as? PlayerSelectionActivity)?.abrirSelectorMusica()
                }
            )
        }
    ) { paddingValues ->
        PlayerSelectionScreen(
            viewModel = viewModel,
            nombreJugador = nombreJugador,
            onPlayerSelected = { nombre, palo ->
                val jugadorActual = viewModel.jugador.value
                if (jugadorActual != null) {
                    jugadorActual.realizarApuesta(palo)
                    jugadorActual.palo = palo
                    viewModel.actualizarJugador(jugadorActual)
                    context.startActivity(Intent(context, GameActivity::class.java).apply {
                        putExtra("jugador_palo", jugadorActual.palo)
                    })
                    (context as? Activity)?.finish()
                } else {
                    Toast.makeText(context, "Error: jugador no encontrado", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun PlayerSelectionScreen(
    viewModel: JugadorViewModel,
    nombreJugador: String,
    onPlayerSelected: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var paloSeleccionado by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val idioma = obtenerIdioma(context)
    val botonApostarImage = if (idioma == "en") R.drawable.boton_bet else R.drawable.boton_apostar
    val textoEligeCaballo = stringResource(R.string.select_your_horse)
    val textoTuApuesta = stringResource(R.string.your_bet)
    val textoSeleccionaCaballo = stringResource(R.string.select_horse_toast)
    val textoSinMonedas = stringResource(R.string.no_coins_toast)

    val palos = listOf("Oros", "Copas", "Espadas", "Bastos")
    val imagenesCaballos = mapOf(
        "Oros" to R.drawable.cab_oros,
        "Copas" to R.drawable.cab_copas,
        "Espadas" to R.drawable.cab_espadas,
        "Bastos" to R.drawable.cab_bastos
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_home),
            contentDescription = "Fondo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(170.dp))

            Text(textoEligeCaballo, fontSize = 36.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                palos.forEach { palo ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { paloSeleccionado = palo }
                    ) {
                        Image(
                            painter = painterResource(id = imagenesCaballos[palo]!!),
                            contentDescription = "Caballo $palo",
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = palo,
                            fontSize = 18.sp,
                            color = if (paloSeleccionado == palo) Color.Green else Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            Text(textoTuApuesta, fontSize = 32.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_coins),
                    contentDescription = "Moneda",
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("20", fontSize = 28.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.weight(3f))

            Image(
                painter = painterResource(id = botonApostarImage),
                contentDescription = if (idioma == "en") "Bet" else "Apostar",
                modifier = Modifier
                    .size(190.dp, 200.dp)
                    .clickable {
                        if (paloSeleccionado == null) {
                            Toast.makeText(
                                context,
                                textoSeleccionaCaballo,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val disposable = viewModel.repository.obtenerJugador(nombreJugador)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ jugadorExistente ->
                                    if (jugadorExistente.monedas <= 0) {
                                        Toast.makeText(context, textoSinMonedas, Toast.LENGTH_SHORT).show()
                                        jugadorExistente.monedas = 20
                                    }

                                    jugadorExistente.realizarApuesta(paloSeleccionado!!)
                                    jugadorExistente.palo = paloSeleccionado!!

                                    viewModel.actualizarJugador(jugadorExistente)

                                    onPlayerSelected(jugadorExistente.nombre, jugadorExistente.palo)

                                }, { error ->
                                    error.printStackTrace()
                                }, {
                                    // No existe el jugador, lo creamos
                                    val nuevoJugador = Jugador(
                                        nombre = nombreJugador,
                                        monedas = 100,
                                        partidas = 0,
                                        victorias = 0,
                                        palo = paloSeleccionado!!
                                    )
                                    nuevoJugador.realizarApuesta(paloSeleccionado!!)
                                    viewModel.insertarJugador(nuevoJugador)

                                    onPlayerSelected(nuevoJugador.nombre, nuevoJugador.palo)
                                })
                        }
                    }
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewPlayerSelectionScreenWithTopBar() {
    val fakeJugador = Jugador(
        nombre = "JugadorDemo",
        monedas = 100,
        partidas = 5,
        victorias = 2,
        palo = "Copas"
    )

    // Repositorio falso solo para el Preview
    val fakeRepository = object : JugadorRepository() {
        override fun obtenerJugador(nombre: String) = Maybe.just(fakeJugador)
        override fun insertarJugador(jugador: Jugador) = Completable.complete()
        override fun actualizarJugador(jugador: Jugador) = Completable.complete()
        override fun actualizarUbicacion(nombre: String, lat: Double, lon: Double) = Completable.complete()
    }

    val fakeViewModel = JugadorViewModel(fakeRepository)

    GoldenHorsesTheme {
        PlayerSelectionScreenWithTopBar(
            context = LocalContext.current,
            viewModel = fakeViewModel,
            nombreJugador = fakeJugador.nombre
        )
    }
}


