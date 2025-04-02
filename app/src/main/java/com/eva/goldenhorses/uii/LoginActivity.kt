package com.eva.goldenhorses.uii

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eva.goldenhorses.R
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.eva.goldenhorses.viewmodel.JugadorViewModel
import com.eva.goldenhorses.viewmodel.JugadorViewModelFactory
import com.eva.goldenhorses.data.AppDatabase
import com.eva.goldenhorses.repository.JugadorRepository
import androidx.compose.material3.TextFieldDefaults
import com.eva.goldenhorses.SessionManager
import com.eva.goldenhorses.data.JugadorDAO
import com.eva.goldenhorses.model.Jugador
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe


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

    // Variable para ajustar posición vertical del campo de texto
    var inputOffset by remember { mutableStateOf(0.dp) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo
            Image(
                painter = painterResource(id = R.drawable.fondo_home),
                contentDescription = "Fondo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen "identificate" arriba
                Image(
                    painter = painterResource(id = R.drawable.identificate),
                    contentDescription = "Identifícate",
                    modifier = Modifier
                        .padding(top = 100.dp)
                        .height(100.dp)
                )

                // Input de nombre con ajuste dinámico
                Column(
                    modifier = Modifier
                        .padding(top = inputOffset)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = nombreJugador,
                        onValueChange = { nombreJugador = it },
                        placeholder = { Text("Nombre del jugador") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                // Botón abajo
                Image(
                    painter = painterResource(id = R.drawable.boton_inicio),
                    contentDescription = "Iniciar",
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .size(180.dp)
                        .clickable {
                            if (nombreJugador.isNotBlank()) {
                                viewModel.comprobarOInsertarJugador(nombreJugador)
                                SessionManager.guardarJugador(context, nombreJugador)
                                onLoginSuccess(nombreJugador)
                            } else {
                                Toast.makeText(context, "Debes introducir tu nombre de usuario", Toast.LENGTH_SHORT).show()
                            }
                        }
                )
            }

        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginScreen() {
    // Fake DAO sin operaciones reales
    val fakeDAO = object : JugadorDAO {
        override fun insertarJugador(jugador: Jugador) = Completable.complete()
        override fun obtenerJugador(nombre: String) = Maybe.empty<Jugador>()
        override fun actualizarJugador(jugador: Jugador) = Completable.complete()
    }

    val fakeRepository = com.eva.goldenhorses.repository.JugadorRepository(fakeDAO)
    val fakeViewModel = com.eva.goldenhorses.viewmodel.JugadorViewModel(fakeRepository)

    GoldenHorsesTheme {
        LoginScreen(
            viewModel = fakeViewModel,
            onLoginSuccess = {}
        )
    }
}

