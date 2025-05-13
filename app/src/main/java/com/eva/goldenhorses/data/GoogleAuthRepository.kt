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
        val userId = auth.currentUser?.uid ?: return // Asegura que no es null
        val jugadorRef = db.collection("jugadores").document(userId)

        val datosBase = mapOf(
            "nombre" to nombre,
            "monedas" to monedas,
            "partidas" to partidas,
            "victorias" to victorias,
            "palo" to palo,
            "latitud" to latitud,
            "longitud" to longitud,
            "victoriasPorDia" to emptyMap<String, Int>()
        )

        jugadorRef.get().addOnSuccessListener { document ->
            jugadorRef.set(datosBase, SetOptions.merge())
                .addOnSuccessListener {
                    println(
                        if (document.exists())
                            "Datos del jugador actualizados en Firestore"
                        else
                            "Jugador guardado exitosamente en Firestore"
                    )
                }
                .addOnFailureListener {
                    println("Error al guardar/actualizar jugador: ${it.message}")
                }
        }
    }
}
