package com.eva.goldenhorses.uii

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.goldenhorses.R
import com.eva.goldenhorses.SessionManager
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.model.JugadorRanking
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.eva.goldenhorses.utils.obtenerIdioma
import com.eva.goldenhorses.utils.obtenerPaisDesdeUbicacion
import com.eva.goldenhorses.viewmodel.RankingViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RankingActivity : ComponentActivity() {
    private val rankingViewModel by viewModels<RankingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoldenHorsesTheme {
                RankingScreenWithTopBar(viewModel = rankingViewModel, context = this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreenWithTopBar(viewModel: RankingViewModel, context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var isMusicMutedState by remember { mutableStateOf(false) }

    val nombreJugador = SessionManager.obtenerJugador(context)
    val ranking by viewModel.ranking.collectAsState()

    // Jugador simulado con lat/lon (necesario para AppTopBar)
    val jugador = ranking.find { it.nombre == nombreJugador }?.let {
        Jugador(
            nombre = it.nombre,
            victorias = 0,
            partidas = 0,
            palo = "Oros",
            monedas = 0,
            latitud = 43.2630,
            longitud = -2.9350
        )
    }

    val pais = remember(jugador) {
        if (jugador?.latitud != null && jugador.longitud != null) {
            obtenerPaisDesdeUbicacion(context, jugador.latitud!!, jugador.longitud!!)
        } else null
    }

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
                jugador = jugador,
                pais = pais,
                onChangeMusicClick = {}
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            RankingScreen(viewModel)
        }
    }
}

@Composable
fun RankingScreen(viewModel: RankingViewModel) {
    val ranking by viewModel.ranking.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val idioma = obtenerIdioma(context)
    val topJugador = ranking.maxByOrNull { it.victoriasHoy }
    val jugadorActual = FirebaseAuth.getInstance().currentUser
    val prefs = context.getSharedPreferences("premios", Context.MODE_PRIVATE)
    val calendarioAyer = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val fechaAyer = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendarioAyer.time)
    val clavePremio = "premio_dado_$fechaAyer"
    var premioReclamado by remember { mutableStateOf(prefs.getBoolean(clavePremio, false)) }
    val puedeReclamar = topJugador?.nombre == jugadorActual?.displayName && !premioReclamado

    if (error != null) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

    val botonVolverInicio = if (idioma == "en") R.drawable.return_boton else R.drawable.volver_inicio
    val botonReclamarPremio = if (idioma == "en") R.drawable.collect_prize else R.drawable.recoger_premio

    Box(modifier = Modifier.fillMaxSize()) {

        if (puedeReclamar) {
            Image(
                painter = painterResource(id = botonReclamarPremio),
                contentDescription = "Botón Obtener Premio",
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(width = 200.dp, height = 64.dp)
                    .clickable {
                        val jugadorActual = FirebaseAuth.getInstance().currentUser
                        if (!premioReclamado) {
                            if (jugadorActual != null && topJugador?.nombre == jugadorActual.displayName) {
                                reclamarPremio(context)
                                prefs.edit().putBoolean(clavePremio, true).apply()
                                premioReclamado = true
                            } else {
                                Toast.makeText(context, "No eres el jugador con más victorias.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Ya has reclamado el premio hoy.", Toast.LENGTH_SHORT).show()
                        }
                    }
            )
        }


        Image(
            painter = painterResource(id = R.drawable.fondo_ranking),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 160.dp, bottom = 32.dp)
                .background(Color.White.copy(alpha = 0.85f), shape = RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(ranking) { jugador ->
                        RankingItem(jugador)
                    }
                }

                Image(
                    painter = painterResource(id = botonVolverInicio),
                    contentDescription = "Botón Volver a Inicio",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(width = 200.dp, height = 64.dp)
                        .clickable {
                            context.startActivity(Intent(context, HomeActivity::class.java))
                        }
                )
            }
        }
    }
}

@Composable
fun RankingItem(jugadorRanking: JugadorRanking) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = jugadorRanking.nombre, fontSize = 12.sp)
            Text(text = "Victorias hoy: ${jugadorRanking.victoriasHoy}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

private fun reclamarPremio(context: Context) {
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid

    if (userId != null) {
        val jugadorRef = db.collection("jugadores").document(userId)
        jugadorRef.update("monedas", FieldValue.increment(120))
            .addOnSuccessListener {
                Toast.makeText(context, "¡Premio de 120 monedas obtenido!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al reclamar el premio.", Toast.LENGTH_SHORT).show()
            }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewRankingScreenWithTopBar() {
    val fakeRanking = listOf(
        JugadorRanking(nombre = "Eva", victoriasHoy = 5),
        JugadorRanking(nombre = "Josep", victoriasHoy = 3),
        JugadorRanking(nombre = "Jordi", victoriasHoy = 1)
    )

    val fakeJugador = Jugador(
        nombre = "Eva",
        monedas = 100,
        partidas = 10,
        victorias = 4,
        palo = "Oros",
        latitud = 43.2630,
        longitud = -2.9350
    )

    GoldenHorsesTheme {
        Scaffold(
            topBar = {
                AppTopBar(
                    context = LocalContext.current,
                    isMusicMuted = false,
                    onToggleMusic = {},
                    jugador = fakeJugador,
                    pais = "España",
                    onChangeMusicClick = {}
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(fakeRanking) { jugadorRanking ->
                        RankingItem(jugadorRanking)
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.volver_inicio),
                    contentDescription = "Botón Obtener Premio",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(width = 200.dp, height = 64.dp)
                )
            }
        }
    }
}
