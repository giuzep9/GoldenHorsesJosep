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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.goldenhorses.R
import com.eva.goldenhorses.MusicService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(context: Context, isMusicMuted: Boolean, onToggleMusic: (Boolean) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { /* Empty title */ },
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
                        onToggleMusic(newState) // Notify parent
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
            containerColor = Color(0xFF000000), // Brownish color
            titleContentColor = Color.White
        ),
        actions = {
            Image(
                painter = painterResource(id = R.drawable.ic_coins),
                contentDescription = "Coins Icon",
                modifier = Modifier.size(24.dp)
            )
            Text(text = "100", color = Color.White, fontSize = 18.sp, modifier = Modifier.padding(8.dp))
        }
    )
}