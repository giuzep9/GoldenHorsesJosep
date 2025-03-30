package com.eva.goldenhorses.uii

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.eva.goldenhorses.viewmodel.JugadorViewModel
import com.eva.goldenhorses.viewmodel.JugadorViewModelFactory
import com.eva.goldenhorses.data.AppDatabase
import com.eva.goldenhorses.repository.JugadorRepository

class LoginActivity : ComponentActivity() {

    private lateinit var jugadorViewModel: JugadorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciar ViewModel con su factory
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = JugadorRepository(database.jugadorDAO())
        val factory = JugadorViewModelFactory(repository)
        jugadorViewModel = factory.create(JugadorViewModel::class.java)

        setContent {
            GoldenHorsesTheme {
                LoginScreen(viewModel = jugadorViewModel) { nombre ->
                    navegarAHOME(nombre)
                }
            }
        }
    }

    private fun navegarAHOME(nombreJugador: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("jugador_nombre", nombreJugador)
        }
        startActivity(intent)
        finish()
    }
}

@Composable
fun LoginScreen(
    viewModel: JugadorViewModel,
    onLoginSuccess: (String) -> Unit
) {
    var nombreJugador by remember { mutableStateOf("") }
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Introduce tu nombre", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = nombreJugador,
                onValueChange = { nombreJugador = it },
                label = { Text("Nombre del jugador") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (nombreJugador.isNotBlank()) {
                        viewModel.comprobarOInsertarJugador(nombreJugador)
                        onLoginSuccess(nombreJugador)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar")
            }
        }
    }
}