package com.eva.goldenhorses.uii

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.eva.goldenhorses.R
import com.eva.goldenhorses.SessionManager
import com.eva.goldenhorses.data.AppDatabase
import com.eva.goldenhorses.data.JugadorDAO
import com.eva.goldenhorses.model.*
import com.eva.goldenhorses.repository.JugadorRepository
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.eva.goldenhorses.utils.aplicarIdioma
import com.eva.goldenhorses.utils.obtenerIdioma
import com.eva.goldenhorses.viewmodel.JugadorViewModel
import com.eva.goldenhorses.viewmodel.JugadorViewModelFactory
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nombreJugador = intent.getStringExtra("jugador_nombre") ?: return
        SessionManager.guardarJugador(this, nombreJugador)


        val database = AppDatabase.getDatabase(applicationContext)
        val repository = JugadorRepository(database.jugadorDAO())
        val factory = JugadorViewModelFactory(repository)
        val jugadorViewModel = factory.create(JugadorViewModel::class.java)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(0, 0, 0, 0)
            insets
        }

        setContent {
            val jugador by jugadorViewModel.jugador.collectAsState()

            LaunchedEffect(nombreJugador) {
                jugadorViewModel.iniciarSesion(nombreJugador)

                val paloDesdeIntent = intent.getStringExtra("jugador_palo")
                if (paloDesdeIntent != null) {
                    jugador?.realizarApuesta(paloDesdeIntent)
                    jugador?.let { jugadorViewModel.actualizarJugador(it) }
                }
            }

            jugador?.let { jugador ->
                GameScreenWithTopBar(jugador = jugador, context = this, viewModel = jugadorViewModel ) {
                    jugadorViewModel.actualizarJugador(jugador)
                }
            }
        }
    }
    override fun attachBaseContext(newBase: Context) {
        val context = aplicarIdioma(newBase) // usa tu función LanguageUtils
        super.attachBaseContext(context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreenWithTopBar(jugador: Jugador, context: Context, viewModel: JugadorViewModel, onGameFinished: () -> Unit) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var isMusicMutedState by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isMusicMutedState = sharedPreferences.getBoolean("isMusicMuted", false)
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
                jugador = jugador
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GameScreen(jugador = jugador, viewModel = viewModel, onGameFinished = onGameFinished)
        }
    }
}


@Composable
fun GameScreen(jugador: Jugador, viewModel: JugadorViewModel, onGameFinished: () -> Unit) {
    var carrera by remember { mutableStateOf(Carrera()) }
    var cartaSacada by remember { mutableStateOf<Carta?>(null) }
    var cartasGiradas by remember { mutableStateOf(mutableSetOf<Int>()) }
    var posicionesCaballos by remember {
        mutableStateOf(carrera.obtenerEstadoCarrera().associate { it.palo to it.posicion })
    }
    var carreraFinalizada by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val idioma = obtenerIdioma(context)
    val botonSacarCartaImage = if (idioma == "en") R.drawable.boton_card else R.drawable.btn_carta

    val imagenesCaballos = mapOf(
        "Oros" to R.drawable.cab_oros,
        "Copas" to R.drawable.cab_copas,
        "Bastos" to R.drawable.cab_bastos,
        "Espadas" to R.drawable.cab_espadas
    )

    LaunchedEffect(carreraFinalizada) {
        if (carreraFinalizada) {
            val ganador = carrera.obtenerGanador()?.palo ?: "Nadie"
            Log.d("DEBUG", "Jugador: $jugador, Palo: ${jugador.palo}, Ganador: $ganador")
            jugador.actualizarMonedas(ganador)
            viewModel.actualizarJugador(jugador)

            // Actualizamos estadísticas del jugador
            jugador.partidas += 1
            if (ganador == jugador.palo) {
                jugador.victorias += 1
            }

            onGameFinished() // Guardar en la base de datos

            val intent = if (jugador.palo == ganador) {
                Intent(context, VictoriaActivity::class.java).apply {
                    putExtra("jugador_palo", jugador.palo)
                    putExtra("jugador_nombre", jugador.nombre)
                }
            } else {
                Intent(context, DerrotaActivity::class.java).apply {
                    putExtra("caballo_ganador", ganador)
                    putExtra("jugador_nombre", jugador.nombre)
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
                    .padding(top = 80.dp),
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

            Spacer(modifier = Modifier.height(65.dp))

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

                            var rotacion by remember { mutableStateOf(0f) }  // Estado para la rotación
                            val rotacionAnimada by animateFloatAsState(
                                targetValue = if (girada) 180f else 0f,  // Si está girada, rota a 180°
                                animationSpec = tween(durationMillis = 500)  // Duración de la animación
                            )

                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .graphicsLayer { rotationY = rotacionAnimada }  // Aplica la rotación en el eje Y
                                    .clickable {
                                        rotacion = if (girada) 0f else 180f  // Invierte la rotación al hacer clic
                                    }
                            ) {
                                if (rotacionAnimada <= 90f) {
                                    // Mostrar el reverso de la carta hasta la mitad del giro
                                    Image(
                                        painter = painterResource(id = R.drawable.imgcarta),
                                        contentDescription = "Carta oculta",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Mostrar la carta revelada en la segunda mitad del giro
                                    Image(
                                        painter = painterResource(id = obtenerImagenCarta(carta)),
                                        contentDescription = "Carta Retroceso",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
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
                    .width(130.dp)
                    .height(65.dp)
                    .fillMaxWidth()
                    .wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                if (!carreraFinalizada) {
                    Image(
                        painter = painterResource(id = botonSacarCartaImage),
                        contentDescription = if (idioma == "en") "Draw Card" else "Sacar Carta",
                        modifier = Modifier
                            .size(200.dp, 80.dp)
                            .wrapContentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                cartaSacada = carrera.sacarCarta()
                                cartaSacada?.let {
                                    // Mover el caballo según la carta extraída
                                    carrera.moverCaballo(it.palo)
                                    posicionesCaballos = carrera.obtenerEstadoCarrera().associate { c -> c.palo to c.posicion }

                                    // Si el juego ya ha finalizado, detén el proceso aquí
                                    if (carrera.esCarreraFinalizada()) {
                                        carreraFinalizada = true
                                        return@clickable // Esto finaliza la ejecución de este bloque (dentro de la lambda del clickable)
                                    }

                                    // Solo si no ha finalizado, aplicar los retrocesos
                                    carrera.obtenerCartasRetroceso().reversed().forEachIndexed { index, carta ->
                                        if (!cartasGiradas.contains(index) && carrera.todosCaballosAlNivel(index + 1)) {
                                            cartasGiradas.add(index)
                                            carrera.retrocederCaballo(carta.palo)
                                            posicionesCaballos = carrera.obtenerEstadoCarrera().associate { c -> c.palo to c.posicion }
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


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewGameScreenWithTopBar() {
    val fakeJugador = Jugador(
        nombre = "JugadorDemo",
        monedas = 200,
        partidas = 5,
        victorias = 2,
        palo = "Oros"
    )

    val fakeDAO = object : JugadorDAO {
        override fun insertarJugador(jugador: Jugador) = Completable.complete()
        override fun obtenerJugador(nombre: String) = Maybe.just(fakeJugador)
        override fun actualizarJugador(jugador: Jugador) = Completable.complete()
        override fun actualizarUbicacion(nombre: String, lat: Double, lon: Double): Completable {
            return Completable.complete()
        }
    }

    val fakeRepository = JugadorRepository(fakeDAO)
    val fakeViewModel = JugadorViewModel(fakeRepository)

    GoldenHorsesTheme {
        GameScreenWithTopBar(
            jugador = fakeJugador,
            context = LocalContext.current,
            viewModel = fakeViewModel,
            onGameFinished = {}
        )
    }
}
