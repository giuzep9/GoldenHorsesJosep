package com.eva.goldenhorses.uii

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.goldenhorses.R
import com.eva.goldenhorses.SessionManager

class DerrotaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nombreJugador = intent.getStringExtra("jugador_nombre") ?: "Jugador"
        val ganador = intent.getStringExtra("caballo_ganador") ?: "Oros"

        setContent {
            DerrotaScreen(caballoGanador = ganador, nombreJugador = nombreJugador)
        }
    }
}

@Composable
fun DerrotaScreen(caballoGanador: String, nombreJugador: String) {
    val context = LocalContext.current
    val icono = when (caballoGanador) {
        "Oros" -> R.drawable.cab_oros
        "Copas" -> R.drawable.cab_copas
        "Espadas" -> R.drawable.cab_espadas
        "Bastos" -> R.drawable.cab_bastos
        else -> R.drawable.mazo
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_derrota),
            contentDescription = "Fondo derrota",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // Contenedor central con fondo semitransparente
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .heightIn(max = 425.dp)
                .background(Color.White.copy(alpha = 0.8f))
                .align(Alignment.Center)
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Has perdido 😢", fontSize = 28.sp)

                Spacer(modifier = Modifier.height(24.dp))

                Image(
                    painter = painterResource(id = icono),
                    contentDescription = "Caballo ganador",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = painterResource(id = R.drawable.volver_jugar),
                    contentDescription = "Volver a Jugar",
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .clickable {
                            val intent = Intent(context, PlayerSelectionActivity::class.java).apply {
                                putExtra("jugador_nombre", nombreJugador)
                            }
                            context.startActivity(intent)
                        }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.volver_inicio),
                    contentDescription = "Volver a Inicio",
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .clickable {
                            SessionManager.guardarJugador(context, nombreJugador)
                            val intent = Intent(context, HomeActivity::class.java)
                            context.startActivity(intent)
                        }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewDerrotaScreen() {
    DerrotaScreen(caballoGanador = "Oros", nombreJugador = "JugadorDemo")
}
