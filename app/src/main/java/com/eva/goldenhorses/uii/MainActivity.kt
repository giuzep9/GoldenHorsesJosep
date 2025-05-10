package com.eva.goldenhorses.uii

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.eva.goldenhorses.MusicService
import com.eva.goldenhorses.R
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoldenHorsesTheme {
                WelcomeScreen { navigateToLogin() }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, GoogleLoginActivity::class.java)
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
            painter = painterResource(id = R.drawable.portada),
            contentDescription = "Fondo de la pantalla de inicio",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // Contenedor para logo y botón
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp), // Ajusta la posición del botón
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Distribuye el logo y el botón
        ) {
            // Logo en la parte superior
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo del videojuego",
                modifier = Modifier
                    .width(450.dp) // Ajusta el tamaño del logo
                    .height(325.dp)
                    .padding(top = 50.dp) // Espacio superior
            )

            // Espaciador para centrar el botón en la parte inferior
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Botón con imagen
                Image(
                    painter = painterResource(id = R.drawable.boton_empezar),
                    contentDescription = "Botón JUGAR",
                    modifier = Modifier
                        .width(500.dp)
                        .height(200.dp)
                        .clickable { onPlayClick() }
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewWelcomeScreen() {
    WelcomeScreen(onPlayClick = {})
}