package com.eva.goldenhorses.uii

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                signInLauncher.launch(
                    IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                )
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fallo al iniciar sesión: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            val nombreGoogle = credential.displayName ?: "Jugador"
            val email = credential.id

            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                            comprobarJugador(uid, nombreGoogle)
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

    private fun comprobarJugador(uid: String, nombreGoogle: String) {
        db.collection("jugadores").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombreJugador = document.getString("nombre") ?: "Jugador"
                    navegarAHome(nombreJugador)
                } else {
                    // No existe → pedir nombre de usuario
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        putExtra("firebase_uid", uid)
                        putExtra("nombre_google", nombreGoogle)
                    }
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error consultando Firestore", Toast.LENGTH_SHORT).show()
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
