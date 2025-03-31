package com.eva.goldenhorses.uii

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.eva.goldenhorses.R
import com.eva.goldenhorses.SessionManager
import com.eva.goldenhorses.data.AppDatabase
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.repository.JugadorRepository
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.eva.goldenhorses.viewmodel.JugadorViewModel
import com.eva.goldenhorses.viewmodel.JugadorViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = JugadorRepository(database.jugadorDAO())
        val factory = JugadorViewModelFactory(repository)
        jugadorViewModel = factory.create(JugadorViewModel::class.java)

        val nombreJugador = intent.getStringExtra("jugador_nombre") ?: ""
        SessionManager.guardarJugador(this, nombreJugador)


        setContent {
            PlayerSelectionScreenWithTopBar(context = this, viewModel = jugadorViewModel, nombreJugador = nombreJugador)
        }
    }
}

@SuppressLint("CommitPrefEdits")
@Composable
fun PlayerSelectionScreenWithTopBar(
    context: Context,
    viewModel: JugadorViewModel,
    nombreJugador: String
)
 {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var isMusicMutedState by remember { mutableStateOf(sharedPreferences.getBoolean("isMusicMuted", false)) }

     val jugadorState = remember { mutableStateOf<Jugador?>(null) }

     LaunchedEffect(nombreJugador) {
         val jugador = viewModel.obtenerJugador(nombreJugador)
         jugadorState.value = jugador
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
                jugador = jugadorState.value
            )
        }
    ) { paddingValues ->
        PlayerSelectionScreen(
            nombreJugador = nombreJugador,
            viewModel = viewModel,
            onPlayerSelected = { nombre, palo ->
                CoroutineScope(Dispatchers.IO).launch {
                    val jugadorExistente = viewModel.obtenerJugador(nombre)
                    val monedasActuales = jugadorExistente?.monedas ?: 100

                    val partidasActuales = jugadorExistente?.partidas ?: 0
                    val victoriasActuales = jugadorExistente?.victorias ?: 0

                    val nuevoJugador = Jugador(
                        nombre = nombre,
                        monedas = monedasActuales,
                        partidas = partidasActuales,
                        victorias = victoriasActuales
                    ).apply {
                        this.palo = palo
                       // this.realizarApuesta(palo)
                    }

                    if (jugadorExistente != null) {
                        viewModel.actualizarJugador(nuevoJugador)
                    } else {
                        viewModel.insertarJugador(nuevoJugador)
                    }

                    // ✅ Guardamos los nuevos datos (con monedas actualizadas) en la base de datos
                    viewModel.actualizarJugador(nuevoJugador)

                    val intent = Intent(context, GameActivity::class.java).apply {
                        putExtra("jugador_nombre", nuevoJugador.nombre)
                        putExtra("jugador_palo", nuevoJugador.palo)
                        putExtra("jugador_monedas", nuevoJugador.monedas)
                    }

                    context.startActivity(intent)
                    (context as? Activity)?.finish()
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

    val palos = listOf("Oros", "Copas", "Bastos", "Espadas")
    val imagenesCaballos = mapOf(
        "Oros" to R.drawable.cab_oros,
        "Copas" to R.drawable.cab_copas,
        "Bastos" to R.drawable.cab_bastos,
        "Espadas" to R.drawable.cab_espadas
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White)
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
            Spacer(modifier = Modifier.height(200.dp))

            Text("Elige tu caballo", fontSize = 36.sp, fontWeight = FontWeight.Bold)

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
                            .clickable {
                                paloSeleccionado = palo
                                // Agregar un Log para verificar si el estado se actualiza correctamente
                                Log.d("PlayerSelection", "Palo seleccionado: $paloSeleccionado")
                            }
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

            Text("Tu apuesta:", fontSize = 32.sp, color = Color.Black)
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
                painter = painterResource(id = R.drawable.boton_apostar),
                contentDescription = "Jugar",
                modifier = Modifier
                    .size(190.dp, 200.dp)
                    .clickable {
                        if (paloSeleccionado == null) {
                            Toast.makeText(context, "Selecciona un caballo", Toast.LENGTH_SHORT).show()
                        } else {
                            // Aquí pasamos los valores de nombreJugador y paloSeleccionado a onPlayerSelected
                            Log.d("PlayerSelection", "Palo antes de pasar a GameActivity: $paloSeleccionado")
                            onPlayerSelected(nombreJugador, paloSeleccionado!!)
                        }
                    }
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPlayerSelectionScreen() {
    val fakeViewModel = remember {
        val fakeDAO = object : com.eva.goldenhorses.data.JugadorDAO {
            override suspend fun insertarJugador(jugador: com.eva.goldenhorses.model.Jugador) {}
            override suspend fun obtenerJugador(nombre: String): com.eva.goldenhorses.model.Jugador? = null
            override suspend fun actualizarJugador(jugador: com.eva.goldenhorses.model.Jugador) {}
        }
        val fakeRepository = com.eva.goldenhorses.repository.JugadorRepository(fakeDAO)
        com.eva.goldenhorses.viewmodel.JugadorViewModel(fakeRepository)
    }

    GoldenHorsesTheme {
        PlayerSelectionScreen(
            viewModel = fakeViewModel,
            nombreJugador = "JugadorDemo",
            onPlayerSelected = { _, _ -> }
        )
    }
}
