package com.eva.goldenhorses.uii

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.eva.goldenhorses.MusicService
import com.eva.goldenhorses.R
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nombreUsuario = intent.getStringExtra("nombre_usuario") ?: "Jugador"

        setContent {
            HomeScreenWithTopBar(this, nombreUsuario)
        }

        // Ensure system bars behavior is properly set
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(0, 0, 0, 0) // Remove any automatic padding
            insets
        }

        // Start background music service
        val musicIntent = Intent(this, MusicService::class.java)
        startService(musicIntent)

        setContent {
            HomeScreenWithTopBar(this, nombreUsuario)
        }
    }
}
@Composable
fun HomeScreenWithTopBar(context: Context, nombreUsuario: String) {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var isMusicMutedState by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isMusicMutedState = sharedPreferences.getBoolean("isMusicMuted", false)
    }

    GoldenHorsesTheme {
        @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold(
            topBar = {
                AppTopBar(
                    context = context,
                    isMusicMuted = isMusicMutedState,
                    onToggleMusic = { newState ->
                        isMusicMutedState = newState
                        sharedPreferences.edit().putBoolean("isMusicMuted", newState).apply()
                    }
                )
            }
        ) { paddingValues ->
            Box( // 🔹 Ensure full background coverage
                modifier = Modifier.fillMaxSize()
            ) {
                HomeScreen(
                    onPlayClick = {
                        context.startActivity(Intent(context, PlayerSelectionActivity::class.java))
                    },
                    nombreUsuario = nombreUsuario,
                    modifier = Modifier.fillMaxSize() // 🔹 Make sure it takes the full size
                )
            }
        }
    }
}

@Composable
fun HomeScreen(onPlayClick: () -> Unit, nombreUsuario: String, modifier: Modifier = Modifier) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Fondo personalizado
        Image(
            painter = painterResource(id = R.drawable.fondo_home),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .matchParentSize(),
            contentScale = ContentScale.Crop
        )

        // Centered Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Icon
            Image(
                painter = painterResource(id = R.drawable.user_icon),
                contentDescription = "User Icon",
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape), // Ensures rounded shape if needed
                contentScale = ContentScale.Fit // Ensures the image fits properly
            )

            // Username
            Text(
                text = nombreUsuario,
                fontSize = 50.sp,
                color = Color.Black,
                modifier = Modifier.padding(10.dp),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            // Crear las variables y la lógica para que se sumen los valores cuando juegues
            // Games Played & Victories
            Text(text = "Número de partidas: 10", fontSize = 18.sp, color = Color.Black, modifier = Modifier.padding(bottom = 10.dp))
            Text(text = "Victorias: 5", fontSize = 18.sp, color = Color.Black, modifier = Modifier.padding(bottom = 32.dp))

            // Play Button
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 90.dp), // Ajusta la posición del botón
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom // Para que el botón esté abajo
            ) {
                // Botón con imagen
                Image(
                    painter = painterResource(id = R.drawable.boton_jugar),
                    contentDescription = "Botón JUGAR",
                    modifier = Modifier
                        .size(200.dp, 80.dp)
                        .clickable { onPlayClick() } // Hace que la imagen sea clickeable
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(
        onPlayClick = {},
        nombreUsuario = "Paco"
    )
}
