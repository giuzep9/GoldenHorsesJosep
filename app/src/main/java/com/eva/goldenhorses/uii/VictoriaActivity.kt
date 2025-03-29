package com.eva.goldenhorses.uii

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.goldenhorses.R

class VictoriaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val caballoPalo = intent.getStringExtra("jugador_palo") ?: "Oros"

        setContent {
            VictoriaScreen(caballoPalo = caballoPalo)
        }
    }
}

@Composable
fun VictoriaScreen(caballoPalo: String) {
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
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_victoria),
            contentDescription = "Fondo victoria",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // Contenedor con fondo blanco al 50% de opacidad, limitado a la mitad de la pantalla
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)  // Asegura que ocupe todo el ancho
                .heightIn(max = 425.dp) // Limita la altura máxima
                .background(Color.White.copy(alpha = 0.8f)) // Fondo blanco con opacidad del 50%

                .align(Alignment.Center) // Centra el contenido
                .padding(32.dp)
        ) {
            // Contenido centralizado
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(2.dp))

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
                        .fillMaxWidth(0.55f) // 80% del ancho disponible
                        // .aspectRatio(f) // Mantiene la proporción cuadrada
                        .clickable { context.startActivity(Intent(context, PlayerSelectionActivity::class.java)) }
                )


                Spacer(modifier = Modifier.height(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.volver_inicio),
                    contentDescription = "Volver a Inicio",
                    modifier = Modifier
                        .fillMaxWidth(0.55f) // 80% del ancho disponible
                        //.aspectRatio(1f) // Mantiene la proporción cuadrada
                        .clickable { context.startActivity(Intent(context, HomeActivity::class.java)) }
                )
                Spacer(modifier = Modifier.width(8.dp)) // Espacio entre la imagen y el texto


            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewVictoriaScreen() {
    VictoriaScreen(caballoPalo = "Oros") // puedes usar cualquier valor válido
}
