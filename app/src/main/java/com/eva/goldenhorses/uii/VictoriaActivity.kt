package com.eva.goldenhorses.uii

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.goldenhorses.R

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
        "Oros" -> R.drawable.imagen_oros
        "Copas" -> R.drawable.imagen_copas
        "Espadas" -> R.drawable.imagen_espadas
        "Bastos" -> R.drawable.imagen_bastos
        else -> R.drawable.mazo
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_victoria),
            contentDescription = "Fondo victoria",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        // Contenido encima del fondo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Acomoda los elementos desde la parte superior
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_victoria),
                contentDescription = "Logo de victoria",
                modifier = Modifier
                    .width(500.dp)  // Ocupará el 100% del ancho de la pantalla
                    .height(300.dp)  // Ajusta la altura de la imagen, puedes modificarla según sea necesario
                    .padding(vertical = 0.dp)  // Espacio adicional entre el logo y otros elementos
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text("¡Has ganado! 🎉", fontSize = 28.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = icono),
                contentDescription = "Tu caballo ganador",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = {
                val intent = Intent(context, PlayerSelectionActivity::class.java).apply {
                    putExtra("jugador_nombre", nombreJugador)
                }
                context.startActivity(intent)
            }) {
                Text("Volver a jugar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val intent = Intent(context, HomeActivity::class.java).apply {
                    putExtra("jugador_nombre", nombreJugador)
                }
                context.startActivity(intent)
            }) {
                Text("Volver a Inicio")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewVictoriaScreen() {
    VictoriaScreen(caballoPalo = "Oros", nombreJugador = "JugadorDemo")
}
