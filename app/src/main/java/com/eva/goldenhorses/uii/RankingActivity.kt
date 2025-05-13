package com.eva.goldenhorses.ui


import android.content.Context
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.goldenhorses.R
import com.eva.goldenhorses.model.JugadorRanking
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
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
                RankingScreen(viewModel = rankingViewModel)
            }
        }
    }
    fun obtenerRankingDelDiaAnterior(callback: (List<JugadorRanking>, String?) -> Unit) {
        val fechaAyer = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.time)
        val db = FirebaseFirestore.getInstance()
        val rankingList = mutableListOf<JugadorRanking>()
        var ganadorDelDia: String? = null

        db.collection("jugadores").get().addOnSuccessListener { jugadoresSnapshot ->
            val total = jugadoresSnapshot.size()
            var procesados = 0

            for (jugadorDoc in jugadoresSnapshot) {
                val nombre = jugadorDoc.id

                jugadorDoc.reference.collection("victoriasPorDia")
                    .document(fechaAyer)
                    .get()
                    .addOnSuccessListener { docDia ->
                        val victoriasAyer = docDia.getLong("victorias")?.toInt() ?: 0
                        if (victoriasAyer > 0) {
                            rankingList.add(JugadorRanking(nombre, victoriasAyer))

                            // Mostrar victorias de ayer para cada jugador
                          //  Log.d("Ranking", "$nombre: Victorias ayer = $victoriasAyer")

                            // Mostrar victorias en un Toast
                            //Toast.makeText(applicationContext, "$nombre: Victorias ayer = $victoriasAyer", Toast.LENGTH_SHORT).show()
                        }

                        procesados++
                        if (procesados == total) {
                            // Ordenar por victorias descendente
                            val rankingOrdenado = rankingList.sortedByDescending { it.victoriasHoy }

                            // Obtener el ganador del día anterior (primer jugador de la lista)
                            ganadorDelDia = rankingOrdenado.firstOrNull()?.nombre

                            // Mostrar el nombre del jugador ganador en un Toast
                            if (ganadorDelDia != null) {
                                Toast.makeText(applicationContext, "Ganador de ayer: $ganadorDelDia", Toast.LENGTH_SHORT).show()
                            }

                            callback(rankingOrdenado, ganadorDelDia)
                        }
                    }
            }
        }
    }


    @Composable
    fun RankingScreen(viewModel: RankingViewModel) {
        val ranking by viewModel.ranking.collectAsState()
        val error by viewModel.error.collectAsState()
        val context = LocalContext.current
        val db = FirebaseFirestore.getInstance()

        var esGanadorDeAyer by remember { mutableStateOf(false) }  // Estado para saber si es ganador

        // Obtener el ranking del día anterior y verificar si eres el ganador
        LaunchedEffect(Unit) {
            obtenerRankingDelDiaAnterior { rankingList, ganadorDelDia ->
                val jugadorActual = FirebaseAuth.getInstance().currentUser
                if (jugadorActual != null) {
                    // Verificar si el jugador actual es el ganador
                    esGanadorDeAyer = ganadorDelDia == jugadorActual.displayName
                }
            }
        }

        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
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

                    // Mostrar el botón solo si el jugador es el ganador de ayer
                    if (esGanadorDeAyer) {
                        val botonPremioDrawable = R.drawable.boton_home

                        Image(
                            painter = painterResource(id = botonPremioDrawable),
                            contentDescription = "Botón Obtener Premio",
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .size(width = 200.dp, height = 64.dp)
                                .clickable {
                                    val jugadorActual = FirebaseAuth.getInstance().currentUser
                                    if (jugadorActual != null) {
                                        // Asignar las 120 monedas al jugador
                                        val jugadorRef = FirebaseFirestore.getInstance().collection("jugadores").document(jugadorActual.uid)
                                        jugadorRef.update("monedas", FieldValue.increment(120)) // Sumar 120 monedas

                                        // Puedes agregar algún mensaje para informar al jugador de que el premio se ha sumado
                                        Toast.makeText(context, "¡Felicidades! Has recibido 120 monedas.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        )
                    }


                }
            }
        }
    }

    /*@Composable
    fun RankingScreen(viewModel: RankingViewModel) {
        val ranking by viewModel.ranking.collectAsState()
        val error by viewModel.error.collectAsState()
        val context = LocalContext.current

        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
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

                    // Botón de reclamar premio
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
    }*/

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
