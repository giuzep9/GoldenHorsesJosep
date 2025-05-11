package com.eva.goldenhorses.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.goldenhorses.R
import com.eva.goldenhorses.model.JugadorRanking
import com.eva.goldenhorses.repository.JugadorRepository
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RankingActivity : ComponentActivity() {

    private val jugadorRepository = JugadorRepository(FirebaseFirestore.getInstance())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoldenHorsesTheme {
                RankingScreen(jugadorRepository)
            }
        }
    }

    @Composable
    fun RankingScreen(jugadorRepository: JugadorRepository) {
        var ranking by remember { mutableStateOf<List<JugadorRanking>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            jugadorRepository.obtenerRankingDelDia { jugadores ->
                ranking = jugadores
                isLoading = false
            }
        }

        if (!isLoading && ranking.isEmpty()) {
            Toast.makeText(context, "No hay jugadores en el ranking.", Toast.LENGTH_SHORT).show()
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo de imagen
            Image(
                painter = painterResource(id = R.drawable.fondo_ranking),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Contenedor blanco translúcido
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
                    // Lista de jugadores
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(ranking) { jugador ->
                            RankingItem(jugador)
                        }
                    }

                    // Botón siempre visible (con o sin premio disponible)
                    val botonPremioDrawable = R.drawable.boton_home

                    Image(
                        painter = painterResource(id = botonPremioDrawable),
                        contentDescription = "Botón Obtener Premio",
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .size(width = 200.dp, height = 64.dp)
                            .clickable {
                                val prefs = context.getSharedPreferences("premios", Context.MODE_PRIVATE)
                                val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                val clavePremio = "premio_dado_$fechaHoy"
                                val premioReclamado = prefs.getBoolean(clavePremio, false)

                                if (!premioReclamado) {
                                    val jugadorActual = FirebaseAuth.getInstance().currentUser
                                    if (jugadorActual != null) {
                                        val jugadorConMasVictorias = ranking.maxByOrNull { it.victoriasHoy }
                                        if (jugadorConMasVictorias?.nombre == jugadorActual.displayName) {
                                            reclamarPremio(context)
                                            prefs.edit().putBoolean(clavePremio, true).apply()
                                        } else {
                                            Toast.makeText(context, "No eres el jugador con más victorias.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Ya has reclamado el premio hoy.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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
                Text(
                    text = jugadorRanking.nombre,
                    fontSize = 12.sp
                )
                Text(
                    text = "Victorias hoy: ${jugadorRanking.victoriasHoy}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
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
}


/*
package com.eva.goldenhorses.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.goldenhorses.model.JugadorRanking
import com.eva.goldenhorses.repository.JugadorRepository
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.google.firebase.firestore.FirebaseFirestore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext


class RankingActivity : ComponentActivity() {

    private val jugadorRepository = JugadorRepository(FirebaseFirestore.getInstance())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoldenHorsesTheme {
                RankingScreen(jugadorRepository)
            }
        }
    }

    @Composable
    fun RankingScreen(jugadorRepository: JugadorRepository) {
        var ranking by remember { mutableStateOf<List<JugadorRanking>>(emptyList()) }
        val context = LocalContext.current

        // Cargar el ranking de manera asincrónica
        LaunchedEffect(Unit) {
            jugadorRepository.obtenerRankingDelDia { jugadores ->
                ranking = jugadores
            }
        }

        // Si no hay jugadores, mostramos un mensaje
        if (ranking.isEmpty()) {
            Toast.makeText(context, "No hay jugadores en el ranking.", Toast.LENGTH_SHORT).show()
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Ranking de Jugadores",
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )

            // Mostramos el ranking en una lista
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(ranking) { jugador ->
                    RankingItem(jugador)
                }
            }
        }
    }

    // Composable que muestra cada ítem del ranking
    @Composable
    fun RankingItem(jugadorRanking: JugadorRanking) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp) // Usar CardDefaults.cardElevation
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = jugadorRanking.nombre,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Victorias hoy: ${jugadorRanking.victoriasHoy}",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

*/
/*
class RankingActivity : ComponentActivity() {

    private val jugadorRepository = JugadorRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val jugadorActual = SesionJugador.jugadorActual

        setContent {
            RankingScreen(jugadorRepository, jugadorActual)
        }
    }
}
@Composable
fun RankingScreen(jugadorRepository: JugadorRepository, jugadorActual: Jugador) {
    val context = LocalContext.current
    var ranking by remember { mutableStateOf<List<JugadorRanking>>(emptyList()) }
    var yaCobroHoy by remember { mutableStateOf(false) }

    // Cargar el ranking y el estado de cobro del premio
    LaunchedEffect(Unit) {
        jugadorRepository.obtenerRankingDelDia { jugadores ->
            ranking = jugadores
        }

        // Verificar si ya cobró el premio hoy
        val prefs = context.getSharedPreferences("ranking_prefs", android.content.Context.MODE_PRIVATE)
        val hoy = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        yaCobroHoy = prefs.getBoolean("cobro_$hoy", false)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo de imagen
        Image(
            painter = painterResource(id = R.drawable.fondo_victory),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Caja blanca translúcida con borde redondeado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .background(Color.White.copy(alpha = 0.85f), shape = RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ranking Diario", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(ranking) { index, jugador ->
                        Text(
                            text = "${index + 1}. ${jugador.nombre} - ${jugador.victoriasHoy} victorias",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                val esPrimerLugar = ranking.firstOrNull()?.nombre == jugadorActual.nombre

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    esPrimerLugar && !yaCobroHoy -> {
                        Button(
                            onClick = {
                                jugadorRepository.actualizarMonedas(jugadorActual.nombre, 120) {
                                    val prefs = context.getSharedPreferences("ranking_prefs", android.content.Context.MODE_PRIVATE)
                                    val hoy = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                                    prefs.edit().putBoolean("cobro_$hoy", true).apply()
                                    Toast.makeText(context, "¡Has recibido 120 monedas!", Toast.LENGTH_LONG).show()
                                    yaCobroHoy = true
                                    // Volver a cargar el ranking actualizado
                                    jugadorRepository.obtenerRankingDelDia { jugadores ->
                                        ranking = jugadores
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Recoger premio (120 monedas)")
                        }
                    }

                    esPrimerLugar -> {
                        Text("Ya has cobrado tu premio hoy", color = Color.Gray)
                    }
                }
            }
        }
    }
}
*/