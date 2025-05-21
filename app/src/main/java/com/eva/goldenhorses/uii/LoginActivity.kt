package com.eva.goldenhorses.uii

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
//import com.eva.goldenhorses.data.AppDatabase
import com.eva.goldenhorses.repository.JugadorRepository
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.res.stringResource
import com.eva.goldenhorses.MusicService
import com.eva.goldenhorses.SessionManager
//import com.eva.goldenhorses.data.JugadorDAO
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.utils.aplicarIdioma
import com.eva.goldenhorses.utils.guardarIdioma
import com.eva.goldenhorses.utils.obtenerIdioma
import com.eva.goldenhorses.utils.restartApp
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe


class LoginActivity : ComponentActivity() {

    private lateinit var jugadorViewModel: JugadorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Iniciar ViewModel con su factory para Firebase
        val repository = JugadorRepository() // Usamos el repositorio que conecta con Firebase
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

    // Cambio del idioma (si lo tienes implementado)
    override fun attachBaseContext(newBase: Context) {
        val context = aplicarIdioma(newBase) // usa tu función LanguageUtils
        super.attachBaseContext(context)
    }

    // Navegar a la pantalla principal (HomeActivity) pasando el nombre del jugador
    private fun navegarAHOME(nombreJugador: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("jugador_nombre", nombreJugador)
        }
        startActivity(intent)
        finish()  // Cierra la actividad de Login una vez que se navega a Home
    }
}
/*class LoginActivity : ComponentActivity() {

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

    override fun attachBaseContext(newBase: Context) {
        val context = aplicarIdioma(newBase) // usa tu función LanguageUtils
        super.attachBaseContext(context)
    }

    private fun navegarAHOME(nombreJugador: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("jugador_nombre", nombreJugador)
        }
        startActivity(intent)
        finish()
    }
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTopBar(
    context: Context,
    isMusicMuted: Boolean,
    onToggleMusic: (Boolean) -> Unit,
    onIdiomaSelected: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(32.dp)
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
                DropdownMenuItem(
                    text = { Text("Español") },
                    onClick = {
                        guardarIdioma(context, "es")
                        restartApp(context)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("English") },
                    onClick = {
                        guardarIdioma(context, "en")
                        restartApp(context)
                        showMenu = false
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF9DC4E3),
            titleContentColor = Color.White
        )
    )
}

@Composable
fun LoginScreen(
    viewModel: JugadorViewModel,
    onLoginSuccess: (String) -> Unit
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

    GoldenHorsesTheme {
        Scaffold(
            topBar = {
                AppTopBar(
                    context = context,
                    isMusicMuted = isMusicMutedState,
                    onToggleMusic = { newState ->
                        isMusicMutedState = newState
                        sharedPreferences.edit().putBoolean("isMusicMuted", newState).apply()
                    },
                    jugador = null,     // No hay monedas ni ubicación
                    pais = null          // No se muestra el icono de país
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Fondo
                Image(
                    painter = painterResource(id = R.drawable.fondo_home),
                    contentDescription = "Fondo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Contenido
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Imagen "identificate"
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
                            placeholder = { Text(text = stringResource(id = R.string.nombre_jugador_placeholder)) },
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

                    // Botón "Inicio"
                    Image(
                        painter = painterResource(id = botonInicioImage),
                        contentDescription = "Iniciar",
                        modifier = Modifier
                            .padding(bottom = 40.dp)
                            .size(180.dp)
                            .clickable {
                                if (nombreJugador.isNotBlank()) {
                                    val currentUser = FirebaseAuth.getInstance().currentUser
                                    if (currentUser != null) {
                                        viewModel.comprobarOInsertarJugador(nombreJugador)
                                        SessionManager.guardarJugador(context, nombreJugador)
                                        onLoginSuccess(nombreJugador)
                                    } else {
                                        Toast.makeText(context, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, context.getString(R.string.introduce_nombre), Toast.LENGTH_SHORT).show()
                                }

                            }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginScreen() {
    // Usamos el repositorio que conecta con Firebase
    val repository = com.eva.goldenhorses.repository.JugadorRepository()
    val fakeViewModel = com.eva.goldenhorses.viewmodel.JugadorViewModel(repository)

    GoldenHorsesTheme {
        LoginScreen(
            viewModel = fakeViewModel,
            onLoginSuccess = {}
        )
    }
}

/*@Composable
fun PreviewLoginScreen() {
    // Fake DAO sin operaciones reales
    val fakeDAO = object : JugadorDAO {
        override fun insertarJugador(jugador: Jugador) = Completable.complete()
        override fun obtenerJugador(nombre: String) = Maybe.empty<Jugador>()
        override fun actualizarJugador(jugador: Jugador) = Completable.complete()
        override fun actualizarUbicacion(nombre: String, lat: Double, lon: Double): Completable {
            return Completable.complete()
        }
    }

    val fakeRepository = com.eva.goldenhorses.repository.JugadorRepository(fakeDAO)
    val fakeViewModel = com.eva.goldenhorses.viewmodel.JugadorViewModel(fakeRepository)

    GoldenHorsesTheme {
        LoginScreen(
            viewModel = fakeViewModel,
            onLoginSuccess = {}
        )
    }
}*/

