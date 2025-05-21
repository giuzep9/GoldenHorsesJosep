package com.eva.goldenhorses.uii

import android.content.Context
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.eva.goldenhorses.R
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.eva.goldenhorses.utils.aplicarIdioma
import com.eva.goldenhorses.utils.obtenerIdioma

class HelpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val idioma = obtenerIdioma(this) // Obtiene el idioma guardado


        setContent {
            GoldenHorsesTheme {
                HelpScreen(onBack = { finish() }, idioma = idioma)
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val context = aplicarIdioma(newBase) // Aplica el idioma al contexto
        super.attachBaseContext(context)
    }
}

@Composable
fun HelpScreen(onBack: () -> Unit, idioma: String) {
    val context = LocalContext.current
    val url = when (idioma) {
        "es" -> "file:///android_asset/help_es.html"
        else -> "file:///android_asset/help_en.html"
    }
    val tituloDrawable = when (idioma) {
        "en" -> R.drawable.titulo_help
        else -> R.drawable.titulo_ayuda
    }

    val volverDrawable = when (idioma) {
        "en" -> R.drawable.return_boton
        else -> R.drawable.volver
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo de imagen
        Image(
            painter = painterResource(id = R.drawable.fondo_ayuda),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(), // Esto asegura que ocupe todo
            contentScale = ContentScale.Crop // Esto escala la imagen para llenar el espacio
        )

        // Contenedor blanco con todo el contenido dentro (título + webview + botón)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .background(Color.White.copy(alpha = 0.85f), shape = RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Image(
                    painter = painterResource(id = tituloDrawable),
                    contentDescription = "Título Ayuda",
                    modifier = Modifier
                        .height(120.dp)
                        .padding(top = 8.dp)
                )

                // WebView en el centro
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {
                            WebView(it).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                loadUrl(url)
                                setBackgroundColor(Color.Transparent.toArgb())
                            }
                        }
                    )
                }

                // Botón volver
                Image(
                    painter = painterResource(id = volverDrawable),
                    contentDescription = "Volver",
                    modifier = Modifier
                        .size(160.dp, 64.dp)
                        .clickable { onBack() }
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHelpScreen() {
    GoldenHorsesTheme {
        Column {
            HelpScreen(onBack = {}, idioma = "es") // Cambiar idioma para ver el cambio
        }
    }
}
