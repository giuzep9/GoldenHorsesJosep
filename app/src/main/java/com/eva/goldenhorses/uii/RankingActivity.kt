package com.eva.goldenhorses.uii

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.zIndex
import com.eva.goldenhorses.R
import com.eva.goldenhorses.SessionManager
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.model.JugadorRanking
import com.eva.goldenhorses.repository.JugadorRepository
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.eva.goldenhorses.utils.obtenerIdioma
import com.eva.goldenhorses.utils.obtenerPaisDesdeUbicacion
import com.eva.goldenhorses.viewmodel.JugadorViewModel
import com.eva.goldenhorses.viewmodel.JugadorViewModelFactory
import com.eva.goldenhorses.viewmodel.RankingViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RankingActivity : ComponentActivity() {

    private val rankingViewModel by viewModels<RankingViewModel>()

    private val jugadorViewModel by viewModels<JugadorViewModel> {
        JugadorViewModelFactory(JugadorRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoldenHorsesTheme {
                // Pasa ambos ViewModels a la pantalla
                RankingScreenWithTopBar(
                    viewModel = rankingViewModel,
                    jugadorViewModel = jugadorViewModel,
                    context = this
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreenWithTopBar(
    viewModel: RankingViewModel,
    jugadorViewModel: JugadorViewModel,
    context: Context
) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var isMusicMutedState by remember { mutableStateOf(false) }

    val jugador by jugadorViewModel.jugador.collectAsState()
    val nombreJugador = SessionManager.obtenerJugador(context)

    LaunchedEffect(nombreJugador) {
        jugadorViewModel.iniciarSesion(nombreJugador)
    }

    val pais = remember(jugador) {
        jugador?.latitud?.let { lat ->
            jugador?.longitud?.let { lon ->
                obtenerPaisDesdeUbicacion(context, lat, lon)
            }
        }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            RankingScreen(viewModel)
        }
    }
}


@Composable
fun RankingScreen(viewModel: RankingViewModel) {
    val context = LocalContext.current
    val idioma = obtenerIdioma(context)
    val jugadorActual = FirebaseAuth.getInstance().currentUser
    val prefs = context.getSharedPreferences("premios", Context.MODE_PRIVATE)

    val calendarioAyer = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val fechaAyer = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendarioAyer.time)
    val clavePremio = "premio_dado_$fechaAyer"
    var premioReclamado by remember { mutableStateOf(prefs.getBoolean(clavePremio, false)) }
    var topJugadorDeAyer by remember { mutableStateOf<JugadorRanking?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarRankingDeAyer { top ->
            topJugadorDeAyer = top
        }
    }

    val puedeReclamar = topJugadorDeAyer?.nombre == jugadorActual?.displayName && !premioReclamado

    val ranking by viewModel.ranking.collectAsState()
    val error by viewModel.error.collectAsState()

    if (error != null) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

    val botonVolverInicio = if (idioma == "en") R.drawable.return_boton else R.drawable.volver_inicio
    val botonReclamarPremio = if (idioma == "en") R.drawable.collect_prize else R.drawable.recoger_premio

    Box(modifier = Modifier.fillMaxSize()) {

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

                if (puedeReclamar) {
                    Log.d("PREMIO", "Mostrando botón porque puede reclamar")
                    Image(
                        painter = painterResource(id = botonReclamarPremio),
                        contentDescription = "Botón Obtener Premio",
                        modifier = Modifier
                            .padding(bottom = 100.dp) // justo encima del botón de volver
                            .size(width = 200.dp, height = 64.dp)
                            .zIndex(1f)                 // prioridad visual por encima de fondo
                            .clickable {
                                val jugadorActual = FirebaseAuth.getInstance().currentUser
                                if (!premioReclamado && jugadorActual != null && topJugadorDeAyer?.nombre == jugadorActual.displayName) {
                                    reclamarPremio(context)
                                    prefs.edit().putBoolean(clavePremio, true).apply()
                                    premioReclamado = true
                                } else {
                                    Toast.makeText(context, "Ya has reclamado el premio hoy.", Toast.LENGTH_SHORT).show()
                                }
                            }
                    )
                }

                // 🔧 Solo para pruebas: botón para resetear clave y volver a ver el botón de premio
                Button(
                    onClick = {
                        prefs.edit().remove(clavePremio).apply()
                        premioReclamado = false
                        Toast.makeText(context, "Clave $clavePremio eliminada", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .padding(top = 80.dp) // Ajusta para que no se solape
                ) {
                    Text(text = "Reset Premio (debug)")
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

        jugadorRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    jugadorRef.update("monedas", FieldValue.increment(120))
                        .addOnSuccessListener {
                            Log.d("PREMIO", "Premio añadido a $userId")
                            Toast.makeText(context, "¡Premio de 120 monedas obtenido!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("PREMIO", "Error al actualizar monedas: ${e.message}", e)
                            Toast.makeText(context, "Error al reclamar el premio.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("PREMIO", "Documento del jugador no existe: $userId")
                    Toast.makeText(context, "Jugador no encontrado en la base de datos.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("PREMIO", "Error al acceder a Firestore: ${e.message}", e)
                Toast.makeText(context, "Error al conectar con la base de datos.", Toast.LENGTH_SHORT).show()
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

                // Simular botón de premio en el preview
                Image(
                    painter = painterResource(id = R.drawable.recoger_premio),
                    contentDescription = "Botón Recoger Premio",
                    modifier = Modifier
                        .padding(16.dp)
                        .size(width = 200.dp, height = 64.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.volver_inicio),
                    contentDescription = "Botón Volver a Inicio",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(width = 200.dp, height = 64.dp)
                )
            }
        }
    }
}
