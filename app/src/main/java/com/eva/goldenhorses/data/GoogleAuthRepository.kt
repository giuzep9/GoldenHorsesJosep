package com.eva.goldenhorses.data.auth

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore



class GoogleAuthRepository(
    private val auth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
    private val db: FirebaseFirestore // Asegúrate de pasar la instancia de Firestore
) {
    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun signOut(onComplete: () -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }

    fun firebaseAuthWithGoogle(idToken: String, monedas: Int, partidas: Int, victorias: Int, palo: String, latitud: Double?, longitud: Double?, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userName = auth.currentUser?.displayName
                    if (userName != null) {
                        // Verifica si el jugador existe en Firestore y actualiza los datos
                        guardarJugadorEnFirestore(userName, monedas, partidas, victorias, palo, latitud, longitud)
                    }
                    onResult(true, userName)
                } else {
                    onResult(false, null)
                }
            }
    }

    private fun guardarJugadorEnFirestore(nombre: String, monedas: Int, partidas: Int, victorias: Int, palo: String, latitud: Double?, longitud: Double?) {
        val jugadorRef = db.collection("jugadores").document(nombre)

        jugadorRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Si el jugador existe, actualiza los datos (por ejemplo, monedas, partidas, etc.)
                val jugadorActualizado = mapOf(
                    "monedas" to monedas,         // Actualiza el valor de monedas
                    "partidas" to partidas,       // Actualiza el valor de partidas
                    "victorias" to victorias,     // Actualiza el valor de victorias
                    "palo" to palo,               // Actualiza el palo
                    "latitud" to latitud,         // Actualiza la latitud
                    "longitud" to longitud        // Actualiza la longitud
                )
                jugadorRef.update(jugadorActualizado).addOnSuccessListener {
                    println("Datos del jugador actualizados en Firestore")
                }.addOnFailureListener {
                    println("Error al actualizar datos del jugador: ${it.message}")
                }
            } else {
                // Si no existe, crea un nuevo jugador
                val nuevoJugador = mapOf(
                    "nombre" to nombre,
                    "monedas" to monedas,         // Valor inicial de monedas
                    "partidas" to partidas,       // Valor inicial de partidas
                    "victorias" to victorias,     // Valor inicial de victorias
                    "palo" to palo,               // Valor inicial de palo
                    "latitud" to latitud,         // Valor inicial de latitud
                    "longitud" to longitud        // Valor inicial de longitud
                )
                jugadorRef.set(nuevoJugador).addOnSuccessListener {
                    println("Jugador guardado exitosamente en Firestore")
                }.addOnFailureListener {
                    println("Error al guardar jugador en Firestore: ${it.message}")
                }
            }
        }
    }


}


/*class GoogleAuthRepository(
    private val auth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient
) {
    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun signOut(onComplete: () -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }

    fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userName = auth.currentUser?.displayName
                    onResult(true, userName)
                } else {
                    onResult(false, null)
                }
            }
    }
}
*/