package com.eva.goldenhorses.uii

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.eva.goldenhorses.MusicService
import com.eva.goldenhorses.R
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import androidx.core.view.WindowInsetsControllerCompat

class PlayerSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure window insets behavior
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(0, 0, 0, 0) // Remove automatic padding
            insets
        }

        setContent {
            PlayerSelectionScreenWithTopBar(this)
        }
    }
}

@SuppressLint("CommitPrefEdits")
@Composable
fun PlayerSelectionScreenWithTopBar(context: Context) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var isMusicMutedState by remember { mutableStateOf(sharedPreferences.getBoolean("isMusicMuted", false)) }

    LaunchedEffect(Unit) {
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

                )
            }
        ) { paddingValues ->
            PlayerSelectionScreen(
                onPlayerSelected = { nombre, palo ->
                    val jugador = Jugador(nombre, palo, 100)
                    jugador.realizarApuesta(palo)

                    val intent = Intent(context, GameActivity::class.java).apply {
                        putExtra("jugador_nombre", jugador.nombre)
                        putExtra("jugador_palo", jugador.palo)
                        putExtra("jugador_monedas", jugador.monedas)
                    }
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSelectionScreen(
    onPlayerSelected: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var nombreJugador by remember { mutableStateOf("") }
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Se usa un fondo base para evitar solapamientos
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_home), // Fondo personalizado
            contentDescription = "Fondo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Se usa un Spacer para desplazar el contenido hacia abajo
            Spacer(modifier = Modifier.height(200.dp)) // Puedes ajustar esta altura según tus necesidades

            Text(
                text = "Elige tu caballo",
                fontSize = 36.sp,
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) // Texto en negrita
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Caballos como botones seleccionables
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                palos.forEach { palo ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { paloSeleccionado = palo } // Seleccionar caballo
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

            // Aquí envuelves el contenido que quieres con el recuadro difuminado
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6F)

                    .background(Color.White.copy(alpha = 0.5f)) // Fondo difuminado
                    .clip(RoundedCornerShape(16.dp)) // Esquinas redondeadas
                    .padding(24.dp) // Padding dentro del Box

            ) {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally, // Centra los elementos dentro del Box
                    verticalArrangement = Arrangement.Center // Centra verticalmente
                ) {
                    Text(
                        text = "Tu apuesta:",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black

                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_coins), // Usa tu imagen de monedas
                            contentDescription = "Moneda",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "20",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.weight(3f))
            // Botón de jugar con imagen personalizada
            Image(
                painter = painterResource(id = R.drawable.boton_apostar),
                contentDescription = "Jugar",
                modifier = Modifier
                    .size(190.dp, 200.dp)
                    .clickable {
                        if (paloSeleccionado == null) {
                            Toast.makeText(context, "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                        } else {
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
    GoldenHorsesTheme {
        PlayerSelectionScreen(onPlayerSelected = { _, _ -> })
    }
}