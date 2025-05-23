package com.eva.goldenhorses.uii

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.goldenhorses.R
import com.eva.goldenhorses.MusicService
import com.eva.goldenhorses.model.Jugador
import android.location.Geocoder
import android.widget.Toast
import com.eva.goldenhorses.utils.cambiarIdioma
import com.eva.goldenhorses.utils.guardarIdioma
import java.util.*
import com.eva.goldenhorses.utils.obtenerPaisDesdeUbicacion
import com.eva.goldenhorses.utils.restartApp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    context: Context,
    isMusicMuted: Boolean,
    onToggleMusic: (Boolean) -> Unit,
    onChangeMusicClick: () -> Unit = {},
    jugador: Jugador? = null,
    pais: String? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showLocationMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { /* Puedes agregar un título si quieres */ },
        navigationIcon = {
            IconButton(onClick = { showMenu = true }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings Icon",
                    modifier = Modifier.size(40.dp)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // MUTE - UNMUTE
                DropdownMenuItem(
                    text = { Text(if (isMusicMuted) "Unmute Music" else "Mute Music") },
                    onClick = {
                        val newState = !isMusicMuted
                        onToggleMusic(newState)
                        showMenu = false

                        val action = if (newState) "MUTE" else "UNMUTE"
                        val intent = Intent(context, MusicService::class.java).apply {
                            this.action = action
                        }
                        context.startService(intent)
                    }
                )
                // CHANGE MUSIC
                DropdownMenuItem(
                    text = { Text("Change Music") },
                    onClick = {
                        showMenu = false
                        onChangeMusicClick()
                    }
                )
                // DEFAULT MUSIC
                DropdownMenuItem(
                    text = { Text("Restore Default Music") },
                    onClick = {
                        showMenu = false
                        val intent = Intent(context, MusicService::class.java).apply {
                            action = "CHANGE_MUSIC"
                            putExtra("MUSIC_URI", "DEFAULT")
                        }
                        context.startService(intent)
                    }
                )
                // Submenú de idiomas
                DropdownMenuItem(
                    text = { Text("Idioma / Language") },
                    onClick = {
                        showLanguageMenu = true
                        showMenu = false
                    }
                )
                // Ayuda
                DropdownMenuItem(
                    text = { Text("Ayuda / Help") },
                    onClick = {
                        showMenu = false
                        val intent = Intent(context, HelpActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            }

            // Menú de idiomas
            DropdownMenu(
                expanded = showLanguageMenu,
                onDismissRequest = { showLanguageMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Español") },
                    onClick = {
                        guardarIdioma(context, "es")
                        cambiarIdioma(context, "es")
                        restartApp(context)
                        showLanguageMenu = false
                        Toast.makeText(context, "Idioma cambiado a Español", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text("English") },
                    onClick = {
                        guardarIdioma(context, "en")
                        cambiarIdioma(context, "en")
                        restartApp(context)
                        showLanguageMenu = false
                        Toast.makeText(context, "Language changed to English", Toast.LENGTH_SHORT).show()
                    }
                )
            }

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF9DC4E3),
            titleContentColor = Color.White
        ),
        actions = {
            if (jugador != null) {

                // Icono de ubicación
                IconButton(onClick = { showLocationMenu = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_ubicacion),
                        contentDescription = "Ubicación",
                        modifier = Modifier.size(40.dp)
                    )
                }

                DropdownMenu(
                    expanded = showLocationMenu,
                    onDismissRequest = { showLocationMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = pais ?: "Ubicación desconocida") },
                        onClick = {
                            showLocationMenu = false
                        }
                    )
                }

                Text(
                    text = "${jugador.monedas}",
                    color = Color(0xFF343F4B),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)

                )
                Image(
                    painter = painterResource(id = R.drawable.ic_coins),
                    contentDescription = "Coins Icon",
                    modifier = Modifier.size(40.dp)
                )

            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewAppTopBar() {
    var isMusicMuted by remember { mutableStateOf(false) }

    val fakeJugador = Jugador(
        nombre = "Jugador1",
        monedas = 100,
        partidas = 5,
        victorias = 2,
        palo = "Oros"
    ).apply {
        latitud = 40.4168  // Madrid, por ejemplo
        longitud = -3.7038
    }

    AppTopBar(
        context = LocalContext.current,
        isMusicMuted = isMusicMuted,
        onToggleMusic = { newState -> isMusicMuted = newState },
        jugador = fakeJugador
    )
}


