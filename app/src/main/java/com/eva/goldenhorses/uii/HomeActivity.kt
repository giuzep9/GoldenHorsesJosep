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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import com.eva.goldenhorses.data.AppDatabase
import com.eva.goldenhorses.data.JugadorDAO
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.repository.JugadorRepository
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.eva.goldenhorses.viewmodel.JugadorViewModel
import com.eva.goldenhorses.viewmodel.JugadorViewModelFactory
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

class HomeActivity : ComponentActivity() {

    private lateinit var jugadorViewModel: JugadorViewModel

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

        // ViewModel
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = JugadorRepository(database.jugadorDAO())
        val factory = JugadorViewModelFactory(repository)
        jugadorViewModel = factory.create(JugadorViewModel::class.java)

        val nombreJugadorIntent = intent.getStringExtra("jugador_nombre")

        if (!nombreJugadorIntent.isNullOrEmpty()) {
            SessionManager.guardarJugador(this, nombreJugadorIntent)
        }

        val nombreJugador = SessionManager.obtenerJugador(this)
        Log.d("HomeActivity", "Nombre obtenido de SessionManager: $nombreJugador")

        setContent {
            HomeScreenWithTopBar(this, jugadorViewModel, nombreJugador)
        }
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
                    jugador = jugador // Pasamos el jugador cargado
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

            Text(text = "Número de partidas: $partidas", fontSize = 18.sp, color = Color.Black)
            Text(text = "Victorias: $victorias", fontSize = 18.sp, color = Color.Black, modifier = Modifier.padding(bottom = 32.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Image(
                    painter = painterResource(id = R.drawable.boton_jugar),
                    contentDescription = "Botón JUGAR",
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
fun PreviewHomeScreen() {
    val fakeJugador = Jugador(
        nombre = "JugadorDemo",
        monedas = 100,
        partidas = 10,
        victorias = 4,
        palo = "Copas"
    )

    val fakeDAO = object : JugadorDAO {
        override fun insertarJugador(jugador: Jugador) = Completable.complete()
        override fun obtenerJugador(nombre: String) = Maybe.just(fakeJugador)
        override fun actualizarJugador(jugador: Jugador) = Completable.complete()
    }

    val fakeRepository = JugadorRepository(fakeDAO)
    val fakeViewModel = JugadorViewModel(fakeRepository)

    GoldenHorsesTheme {
        HomeScreenWithTopBar(
            context = LocalContext.current,
            viewModel = fakeViewModel,
            nombreJugador = fakeJugador.nombre
        )
    }
}


