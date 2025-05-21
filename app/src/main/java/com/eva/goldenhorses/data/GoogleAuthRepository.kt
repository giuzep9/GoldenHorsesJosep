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

    fun firebaseAuthWithGoogle(
        idToken: String,
        monedas: Int,
        partidas: Int,
        victorias: Int,
        palo: String,
        latitud: Double?,
        longitud: Double?,
        onResult: (Boolean, String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userName = auth.currentUser?.displayName
                    if (userName != null) {
                        // Verifica si el jugador existe en Firestore y actualiza los datos
                        guardarJugadorEnFirestore(
                            userName,
                            monedas,
                            partidas,
                            victorias,
                            palo,
                            latitud,
                            longitud
                        )
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
        val userId = auth.currentUser?.uid ?: return
        val jugadorRef = db.collection("jugadores").document(userId)

        jugadorRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                // Solo crear si no existe
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

                jugadorRef.set(datosBase)
                    .addOnSuccessListener {
                        println("Jugador creado en Firestore")
                    }
                    .addOnFailureListener {
                        println("Error al crear jugador: ${it.message}")
                    }
            } else {
                println("Jugador ya existe, no se sobrescriben los datos")
            }
        }.addOnFailureListener {
            println("Error al acceder al documento del jugador: ${it.message}")
        }
    }
}
