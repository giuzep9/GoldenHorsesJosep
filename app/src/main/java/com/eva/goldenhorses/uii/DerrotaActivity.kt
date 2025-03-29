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

class DerrotaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ganador = intent.getStringExtra("caballo_ganador") ?: "Oros"

        setContent {
            DerrotaScreen(caballoGanador = ganador)
        }
    }
}

@Composable
fun DerrotaScreen(caballoGanador: String) {
    val context = LocalContext.current
    val icono = when (caballoGanador) {
        "Oros" -> R.drawable.cab_oros
        "Copas" -> R.drawable.cab_copas
        "Espadas" -> R.drawable.cab_espadas
        "Bastos" -> R.drawable.cab_bastos
        else -> R.drawable.mazo
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_derrota),
            contentDescription = "Fondo derrota",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // Contenido encima del fondo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
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

            Button(onClick = {
                context.startActivity(Intent(context, PlayerSelectionActivity::class.java))
            }) {
                Text("Volver a jugar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                context.startActivity(Intent(context, HomeActivity::class.java))
            }) {
                Text("Volver a Inicio")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDerrotaScreen() {
    DerrotaScreen(caballoGanador = "Oros") // puedes usar cualquier valor válido
}