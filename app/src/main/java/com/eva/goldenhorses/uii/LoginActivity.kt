package com.eva.goldenhorses.uii

import android.app.Activity
import android.content.Context
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eva.goldenhorses.R
import com.eva.goldenhorses.SessionManager
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.repository.JugadorRepository
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.eva.goldenhorses.utils.*
import com.eva.goldenhorses.viewmodel.JugadorViewModel
import com.eva.goldenhorses.viewmodel.JugadorViewModelFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class LoginActivity : ComponentActivity() {

    private lateinit var jugadorViewModel: JugadorViewModel
    private var firebaseUid: String? = null
    private var nombreGoogle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recuperamos el UID y nombre de Google desde el intent
        firebaseUid = intent.getStringExtra("firebase_uid")
        nombreGoogle = intent.getStringExtra("nombre_google")

        if (firebaseUid == null) {
            Toast.makeText(this, "Error: UID no proporcionado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val repository = JugadorRepository()
        val factory = JugadorViewModelFactory(repository)
        jugadorViewModel = factory.create(JugadorViewModel::class.java)

        setContent {
            GoldenHorsesTheme {
                LoginScreen(
                    viewModel = jugadorViewModel,
                    onLoginSuccess = { nombre -> navegarAHOME(nombre) },
                    firebaseUid = firebaseUid!!,
                    nombreGoogle = nombreGoogle
                )
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

    override fun attachBaseContext(newBase: Context) {
        val context = aplicarIdioma(newBase)
        super.attachBaseContext(context)
    }
}

@Composable
fun LoginScreen(
    viewModel: JugadorViewModel,
    onLoginSuccess: (String) -> Unit,
    firebaseUid: String,
    nombreGoogle: String?
) {
    var nombreJugador by remember { mutableStateOf("") }
    val context = LocalContext.current

    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var isMusicMutedState by remember { mutableStateOf(false) }

    val idioma = obtenerIdioma(context)
    val identificateImage = if (idioma == "en") R.drawable.login else R.drawable.identificate
    val botonInicioImage = if (idioma == "en") R.drawable.boton_start else R.drawable.boton_inicio

    LaunchedEffect(Unit) {
        isMusicMutedState = sharedPreferences.getBoolean("isMusicMuted", false)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                context = context,
                isMusicMuted = isMusicMutedState,
                onToggleMusic = { newState ->
                    isMusicMutedState = newState
                    sharedPreferences.edit().putBoolean("isMusicMuted", newState).apply()
                },
                jugador = null,
                pais = null
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                Image(
                    painter = painterResource(id = identificateImage),
                    contentDescription = "Identifícate",
                    modifier = Modifier
                        .padding(top = 80.dp)
                        .height(100.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = nombreJugador,
                        onValueChange = { nombreJugador = it },
                        placeholder = {
                            Text(text = stringResource(id = R.string.nombre_jugador_placeholder))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
                            focusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Image(
                    painter = painterResource(id = botonInicioImage),
                    contentDescription = "Iniciar",
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .size(180.dp)
                        .clickable {
                            if (nombreJugador.isNotBlank()) {
                                SessionManager.guardarJugador(context, nombreJugador)

                                viewModel.crearJugador(firebaseUid, nombreJugador)
                                onLoginSuccess(nombreJugador)

                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.introduce_nombre),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                )
            }
        }
    }
}
