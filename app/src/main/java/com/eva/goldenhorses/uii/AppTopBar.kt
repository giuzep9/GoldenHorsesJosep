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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    context: Context,
    isMusicMuted: Boolean,
    onToggleMusic: (Boolean) -> Unit,
    jugador: Jugador? = null
) {
    var showMenu by remember { mutableStateOf(false) }

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
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF9DC4E3),
            titleContentColor = Color.White
        ),
        actions = {
            if (jugador != null) {

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

    AppTopBar(
        context = LocalContext.current,
        isMusicMuted = isMusicMuted,
        onToggleMusic = { newState -> isMusicMuted = newState },
        jugador = Jugador(nombre = "Jugador1", monedas = 100, palo = "Oros") // Ahora pasamos también el valor de palo
    )
}
