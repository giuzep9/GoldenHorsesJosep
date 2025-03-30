package com.eva.goldenhorses.uii

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.eva.goldenhorses.R
import com.eva.goldenhorses.data.AppDatabase
import com.eva.goldenhorses.model.*
import com.eva.goldenhorses.repository.JugadorRepository
import com.eva.goldenhorses.viewmodel.JugadorViewModel
import com.eva.goldenhorses.viewmodel.JugadorViewModelFactory

class GameActivity : ComponentActivity() {

    private lateinit var jugadorViewModel: JugadorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔸 Recuperar el nombre del jugador del intent
        val nombreJugador = intent.getStringExtra("jugador_nombre") ?: return

        // 🔸 Inicializar ViewModel
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = JugadorRepository(database.jugadorDAO())
        val factory = JugadorViewModelFactory(repository)
        jugadorViewModel = factory.create(JugadorViewModel::class.java)

        // Ensure system bars behavior is properly set
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(0, 0, 0, 0) // Remove any automatic padding
            insets
        }

        setContent {
            val jugadorState = remember { mutableStateOf<Jugador?>(null) }

            // 🔸 Cargar el jugador desde la base de datos
            LaunchedEffect(nombreJugador) {
                val jugador = jugadorViewModel.obtenerJugador(nombreJugador)
                jugadorState.value = jugador
            }

            // 🔸 Si el jugador está listo, mostramos la pantalla
            jugadorState.value?.let { jugador ->
                GameScreenWithTopBar(jugador = jugador, context = this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreenWithTopBar(jugador: Jugador, context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var isMusicMutedState by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isMusicMutedState = sharedPreferences.getBoolean("isMusicMuted", false)
    }

    @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold(
        topBar = {
            AppTopBar(
                context = context,
                isMusicMuted = isMusicMutedState,
                onToggleMusic = { newState ->
                    isMusicMutedState = newState
                    sharedPreferences.edit().putBoolean("isMusicMuted", newState).apply()
                },
                jugador = jugador
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            GameScreen(jugador) // Llamada a GameScreen dentro de GameScreenWithTopBar
        }
    }
}
@Composable
fun GameScreen(jugador: Jugador) {
    var carrera by remember { mutableStateOf(Carrera()) }
    var cartaSacada by remember { mutableStateOf<Carta?>(null) }
    var cartasGiradas by remember { mutableStateOf(mutableSetOf<Int>()) }
    var posicionesCaballos by remember {
        mutableStateOf(carrera.obtenerEstadoCarrera().associate { it.palo to it.posicion })
    }
    var carreraFinalizada by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagenesCaballos = mapOf(
        "Oros" to R.drawable.imagen_oros,
        "Copas" to R.drawable.imagen_copas,
        "Bastos" to R.drawable.imagen_bastos,
        "Espadas" to R.drawable.imagen_espadas
    )

    LaunchedEffect(carreraFinalizada) {
        if (carreraFinalizada) {
            val ganador = carrera.obtenerGanador()?.palo ?: "Nadie"
            jugador.actualizarMonedas(ganador)

            val intent = if (jugador.palo == ganador) {
                Intent(context, VictoriaActivity::class.java).apply {
                    putExtra("jugador_palo", jugador.palo)
                }
            } else {
                Intent(context, DerrotaActivity::class.java).apply {
                    putExtra("caballo_ganador", ganador)
                }
            }

            context.startActivity(intent)
            (context as? Activity)?.finish()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.pantalla_carrera),
            contentDescription = "Fondo de la pista",
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mazo y carta sacada
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 110.dp),
                horizontalArrangement = Arrangement.End
            ) {
                cartaSacada?.let {
                    Image(
                        painter = painterResource(id = obtenerImagenCarta(it)),
                        contentDescription = "Carta Jugada",
                        modifier = Modifier.size(70.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Image(
                    painter = painterResource(id = R.drawable.mazo),
                    contentDescription = "Mazo",
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(90.dp))

            // Zona central (carrera)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Cartas de retroceso
                    Column(
                        modifier = Modifier.padding(end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        carrera.obtenerCartasRetroceso().forEachIndexed { index, carta ->
                            val posicionInversa = (carrera.obtenerCartasRetroceso().size - 1) - index
                            val girada = cartasGiradas.contains(posicionInversa)

                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(500)) + scaleIn(),
                                exit = fadeOut(tween(500)) + scaleOut()
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = if (girada) obtenerImagenCarta(carta) else R.drawable.imgcarta
                                    ),
                                    contentDescription = "Carta Retroceso",
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    }

                    // Caballos en pista
                    val cartasRetroceso = carrera.obtenerCartasRetroceso()
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(top = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(modifier = Modifier.height((cartasRetroceso.size).dp))

                        for (nivel in cartasRetroceso.indices) {
                            val nivelInvertido = cartasRetroceso.size - 1 - nivel

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(Alignment.Start)
                                    .offset(x = 45.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                posicionesCaballos.forEach { (palo, posicion) ->
                                    if (posicion == nivelInvertido + 1 && nivelInvertido < cartasRetroceso.size) {
                                        imagenesCaballos[palo]?.let { imagenId ->
                                            Image(
                                                painter = painterResource(id = imagenId),
                                                contentDescription = "Caballo $palo",
                                                modifier = Modifier.size(50.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(50.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Caballos al inicio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.Start)
                    .offset(x = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                posicionesCaballos.keys.forEach { palo ->
                    Image(
                        painter = painterResource(id = obtenerImagenCarta(Carta(palo, 11))),
                        contentDescription = "Caballo $palo",
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón sacar carta
            Box(
                modifier = Modifier
                    .width(130.dp) // Limita el ancho del Box para que no afecte otros elementos
                    .height(65.dp)
                    .fillMaxWidth()
                    .wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                if (!carreraFinalizada) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_carta),
                        contentDescription = "Sacar Carta",
                        modifier = Modifier
                            .size(200.dp, 80.dp) // Ajusta el tamaño según sea necesario
                            .wrapContentSize() // Evita que el botón se expanda más de su contenido
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() }, // Evita el efecto visual
                                indication = null // Elimina la animación de clic
                            ) {
                                cartaSacada = carrera.sacarCarta()
                                cartaSacada?.let {
                                    carrera.moverCaballo(it.palo)
                                    posicionesCaballos = carrera.obtenerEstadoCarrera()
                                        .associate { c -> c.palo to c.posicion }

                                    carrera.obtenerCartasRetroceso().reversed()
                                        .forEachIndexed { index, carta ->
                                            if (!cartasGiradas.contains(index) && carrera.todosCaballosAlNivel(
                                                    index + 1
                                                )
                                            ) {
                                                cartasGiradas.add(index)
                                                carrera.retrocederCaballo(carta.palo)
                                                posicionesCaballos = carrera.obtenerEstadoCarrera()
                                                    .associate { c -> c.palo to c.posicion }
                                            }
                                        }

                                    if (carrera.esCarreraFinalizada()) {
                                        carreraFinalizada = true
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}


// Animación del caballo moviéndose en el eje Y
@Composable
fun AnimatedCaballo(caballo: Caballo) {
    val animatedOffset by animateDpAsState(
        targetValue = (-caballo.posicion * 60).dp, // Movimiento animado en el eje Y
        animationSpec = tween(durationMillis = 1000)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset(y = animatedOffset) // Se mueve de forma fluida en el eje Y
            .padding(4.dp)
    ) {
        Image(
            painter = painterResource(id = obtenerImagenCarta(Carta(caballo.palo, 11))),
            contentDescription = "Caballo ${caballo.palo}",
            modifier = Modifier.size(60.dp)
        )
    }
}

// Función para reiniciar el juego
fun reiniciarJuego(context: Context) {
    val intent = Intent(context, PlayerSelectionActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}

// Función para obtener la imagen de una carta
fun obtenerImagenCarta(carta: Carta): Int {
    val nombreRecurso = "carta_${carta.palo.lowercase()}_${carta.valor}"
    val resId = when (nombreRecurso) {
        "carta_oros_1" -> R.drawable.carta_oros_1
        "carta_oros_2" -> R.drawable.carta_oros_2
        "carta_oros_3" -> R.drawable.carta_oros_3
        "carta_oros_4" -> R.drawable.carta_oros_4
        "carta_oros_5" -> R.drawable.carta_oros_5
        "carta_oros_6" -> R.drawable.carta_oros_6
        "carta_oros_7" -> R.drawable.carta_oros_7
        "carta_oros_10" -> R.drawable.carta_oros_10
        "carta_oros_11" -> R.drawable.caballo_oros
        "carta_oros_12" -> R.drawable.carta_oros_12
        "carta_copas_1" -> R.drawable.carta_copas_1
        "carta_copas_2" -> R.drawable.carta_copas_2
        "carta_copas_3" -> R.drawable.carta_copas_3
        "carta_copas_4" -> R.drawable.carta_copas_4
        "carta_copas_5" -> R.drawable.carta_copas_5
        "carta_copas_6" -> R.drawable.carta_copas_6
        "carta_copas_7" -> R.drawable.carta_copas_7
        "carta_copas_10" -> R.drawable.carta_copas_10
        "carta_copas_11" -> R.drawable.caballo_copas
        "carta_copas_12" -> R.drawable.carta_copas_12
        "carta_espadas_1" -> R.drawable.carta_espadas_1
        "carta_espadas_2" -> R.drawable.carta_espadas_2
        "carta_espadas_3" -> R.drawable.carta_espadas_3
        "carta_espadas_4" -> R.drawable.carta_espadas_4
        "carta_espadas_5" -> R.drawable.carta_espadas_5
        "carta_espadas_6" -> R.drawable.carta_espadas_6
        "carta_espadas_7" -> R.drawable.carta_espadas_7
        "carta_espadas_10" -> R.drawable.carta_espadas_10
        "carta_espadas_11" -> R.drawable.caballo_espadas
        "carta_espadas_12" -> R.drawable.carta_espadas_12
        "carta_bastos_1" -> R.drawable.carta_bastos_1
        "carta_bastos_2" -> R.drawable.carta_bastos_2
        "carta_bastos_3" -> R.drawable.carta_bastos_3
        "carta_bastos_4" -> R.drawable.carta_bastos_4
        "carta_bastos_5" -> R.drawable.carta_bastos_5
        "carta_bastos_6" -> R.drawable.carta_bastos_6
        "carta_bastos_7" -> R.drawable.carta_bastos_7
        "carta_bastos_10" -> R.drawable.carta_bastos_10
        "carta_bastos_11" -> R.drawable.caballo_bastos
        "carta_bastos_12" -> R.drawable.carta_bastos_12
        else -> R.drawable.mazo // Imagen por defecto si no encuentra la carta
    }
    return resId
}
@Preview(showBackground = true)
@Composable
fun PreviewGameScreenWithTopBar() {
    // Jugador de prueba
    val jugadorDemo = Jugador(
        nombre = "JugadorDemo",
        monedas = 100,
        partidas = 5,
        victorias = 2,
        palo = "Oros"
    )

    // Contexto simulado solo para vista previa (no se usa realmente)
    val fakeContext = LocalContext.current

    GameScreenWithTopBar(jugador = jugadorDemo, context = fakeContext)
}
