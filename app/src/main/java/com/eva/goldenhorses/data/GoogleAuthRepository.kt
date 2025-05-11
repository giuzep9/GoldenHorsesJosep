package com.eva.goldenhorses.data.auth

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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

    private fun guardarJugadorEnFirestore(
        nombre: String,
        monedas: Int,
        partidas: Int,
        victorias: Int,
        palo: String,
        latitud: Double?,
        longitud: Double?
    ) {
        val userId = auth.currentUser?.uid // Obtener el UID del usuario
        val jugadorRef = db.collection("jugadores").document(userId!!) // Usar el UID como documento

        jugadorRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Actualizar solo los campos comunes
                val jugadorActualizado = mapOf(
                    "monedas" to monedas,
                    "partidas" to partidas,
                    "victorias" to victorias,
                    "palo" to palo,
                    "latitud" to latitud,
                    "longitud" to longitud
                )
                jugadorRef.set(jugadorActualizado, SetOptions.merge()) // Usa merge para evitar sobrescribir todo
                    .addOnSuccessListener {
                        println("Datos del jugador actualizados en Firestore")
                    }
                    .addOnFailureListener {
                        println("Error al actualizar datos del jugador: ${it.message}")
                    }
            } else {
                // Crear jugador nuevo
                val nuevoJugador = mapOf(
                    "nombre" to nombre,
                    "monedas" to monedas,
                    "partidas" to partidas,
                    "victorias" to victorias,
                    "palo" to palo,
                    "latitud" to latitud,
                    "longitud" to longitud
                )
                jugadorRef.set(nuevoJugador, SetOptions.merge()) // Usa merge para no sobrescribir todo
                    .addOnSuccessListener {
                        println("Jugador guardado exitosamente en Firestore")
                    }
                    .addOnFailureListener {
                        println("Error al guardar jugador en Firestore: ${it.message}")
                    }
            }
        }
    }
}
