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

class VictoriaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nombreJugador = intent.getStringExtra("jugador_nombre") ?: "Jugador"
        val caballoPalo = intent.getStringExtra("jugador_palo") ?: "Oros"

        setContent {
            VictoriaScreen(caballoPalo = caballoPalo, nombreJugador = nombreJugador)
        }
    }
}

@Composable
fun VictoriaScreen(caballoPalo: String, nombreJugador: String) {
    val context = LocalContext.current

    val icono = when (caballoPalo) {
        "Oros" -> R.drawable.cab_oros
        "Copas" -> R.drawable.cab_copas
        "Espadas" -> R.drawable.cab_espadas
        "Bastos" -> R.drawable.cab_bastos
        else -> R.drawable.mazo
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_victoria),
            contentDescription = "Fondo victoria",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // Caja de contenido centrado con fondo blanco semitransparente
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
                Text("¡Has ganado!", fontSize = 28.sp)

                Spacer(modifier = Modifier.height(24.dp))

                Image(
                    painter = painterResource(id = icono),
                    contentDescription = "Tu caballo ganador",
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
fun PreviewVictoriaScreen() {
    VictoriaScreen(caballoPalo = "Oros", nombreJugador = "JugadorDemo")
}
