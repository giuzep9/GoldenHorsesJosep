package com.eva.goldenhorses.uii

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.eva.goldenhorses.uii.HomeActivity
import com.eva.goldenhorses.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class GoogleLoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var nombreJugador: String
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nombreJugador = intent.getStringExtra("nombre_jugador") ?: run {
            Toast.makeText(this, "Nombre de jugador no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        auth = FirebaseAuth.getInstance()
        oneTapClient = Identity.getSignInClient(this)

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        iniciarLogin()
    }

    private fun iniciarLogin() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                signInLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fallo al iniciar sesión: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken

            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            guardarJugadorEnFirestore(nombreJugador)
                        } else {
                            Toast.makeText(this, "Error al autenticar con Firebase", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al recuperar credencial", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarJugadorEnFirestore(nombre: String) {
        val jugadorRef = db.collection("jugadores").document(nombre)
        jugadorRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val nuevoJugador = hashMapOf(
                    "nombre" to nombre,
                    "monedas" to 100,
                    "partidas" to 0,
                    "victorias" to 0,
                    "palo" to "Oros",
                    "latitud" to null,
                    "longitud" to null
                )
                jugadorRef.set(nuevoJugador).addOnSuccessListener {
                    navegarAHome(nombre)
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al guardar jugador en Firestore", Toast.LENGTH_SHORT).show()
                }
            } else {
                navegarAHome(nombre)
            }
        }
    }

    private fun navegarAHome(nombreJugador: String) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("jugador_nombre", nombreJugador)
        }
        startActivity(intent)
        finish()
    }
}
