package com.eva.goldenhorses.uii

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.eva.goldenhorses.R
import com.eva.goldenhorses.ui.theme.GoldenHorsesTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001 // Código para identificar el resultado del intent de inicio de sesión

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoldenHorsesTheme {
                WelcomeScreen { navigateToLogin() }
            }
        }

        // Paso 1: Configura GoogleSignInClient
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Obtén el ID de cliente desde strings.xml
            .requestEmail()  // Solicitamos también la dirección de correo electrónico del usuario
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun navigateToLogin() {
        // Llama a signOut para eliminar cualquier cuenta previamente autenticada
        googleSignInClient.signOut().addOnCompleteListener {
            // Ahora, lanza el intento de inicio de sesión
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    // Procesa el resultado y autentica con Firebase
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.result
            if (account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                // Autentica con Firebase
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = FirebaseAuth.getInstance().currentUser
                            // Login exitoso, puedes continuar con el juego
                            Toast.makeText(this, "¡Bienvenido ${user?.displayName}!", Toast.LENGTH_SHORT).show()

                            // Después de la autenticación exitosa, navega a la siguiente pantalla (HomeActivity)
                            val intent = Intent(this, HomeActivity::class.java)  // Cambia HomeActivity por la actividad de inicio
                            startActivity(intent)
                            finish() // Finaliza esta actividad para no regresar a ella
                        } else {
                            // Maneja el error si no se pudo autenticar
                            Toast.makeText(this, "Error en el inicio de sesión", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Error: el usuario no ha seleccionado ninguna cuenta
                Toast.makeText(this, "No se seleccionó ninguna cuenta", Toast.LENGTH_SHORT).show()
            }
        }
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
