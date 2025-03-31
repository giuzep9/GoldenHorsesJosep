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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.repository.JugadorRepository
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.eva.goldenhorses.viewmodel.JugadorViewModel
import com.eva.goldenhorses.viewmodel.JugadorViewModelFactory

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

        // Obtener SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

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
    var jugador by remember { mutableStateOf<Jugador?>(null) }

    LaunchedEffect(nombreJugador) {
        jugador = viewModel.obtenerJugador(nombreJugador)
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
                    jugador = jugador // ✅ Le pasamos el jugador cargado
                )
            }
        ) { paddingValues ->
            HomeScreen(
                viewModel = viewModel,
                nombreJugador = nombreJugador,
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
    viewModel: JugadorViewModel,
    nombreJugador: String,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var partidas by remember { mutableStateOf(0) }
    var victorias by remember { mutableStateOf(0) }

    LaunchedEffect(nombreJugador) {
        val jugador = viewModel.obtenerJugador(nombreJugador)
        partidas = jugador?.partidas ?: 0
        victorias = jugador?.victorias ?: 0
    }

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
                .padding(top = 180.dp),
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



@Preview(showBackground = true)
@Composable
fun PreviewHomeScreenWithTopBar() {
    // Fake ViewModel y repositorio como antes
    val fakeDAO = object : com.eva.goldenhorses.data.JugadorDAO {
        override suspend fun insertarJugador(jugador: com.eva.goldenhorses.model.Jugador) {}
        override suspend fun obtenerJugador(nombre: String): com.eva.goldenhorses.model.Jugador? {
            return com.eva.goldenhorses.model.Jugador(nombre = nombre, monedas = 100, partidas = 3, victorias = 2)
        }
        override suspend fun actualizarJugador(jugador: com.eva.goldenhorses.model.Jugador) {}
    }

    val fakeRepository = com.eva.goldenhorses.repository.JugadorRepository(fakeDAO)
    val fakeViewModel = com.eva.goldenhorses.viewmodel.JugadorViewModel(fakeRepository)

    val fakeContext = androidx.compose.ui.platform.LocalContext.current

    GoldenHorsesTheme {
        HomeScreenWithTopBar(
            context = fakeContext,
            viewModel = fakeViewModel,
            nombreJugador = "JugadorDemo"
        )
    }
}


