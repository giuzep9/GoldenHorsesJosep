package com.eva.goldenhorses.uii

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eva.goldenhorses.R
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoldenHorsesTheme {
                WelcomeScreen { navigateToHome() }
            }
        }
    }

    private fun navigateToPlayerSelection() {
        val intent = Intent(this, PlayerSelectionActivity::class.java)
        startActivity(intent)
    }
    private fun navigateToHome() { // Function was missing before
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun WelcomeScreen(onPlayClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_inicio),
            contentDescription = "Fondo de la pantalla de inicio",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop // Ajusta la imagen para llenar la pantalla
        )

        // Contenedor para el botón
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp), // Ajusta la posición del botón
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom // Para que el botón esté abajo
        ) {
            // Botón con imagen
            Image(
                painter = painterResource(id = R.drawable.boton_empezar),
                contentDescription = "Botón JUGAR",
                modifier = Modifier
                    .width(250.dp) // Ajusta el tamaño según la imagen
                    .height(100.dp)
                    .clickable { onPlayClick() } // Hace que la imagen sea clickeable
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewWelcomeScreen() {
    WelcomeScreen(onPlayClick = {})
}