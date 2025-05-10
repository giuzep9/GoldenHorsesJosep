package com.eva.goldenhorses.repository

import com.eva.goldenhorses.model.Jugador
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

open class JugadorRepository {

    private val db = FirebaseFirestore.getInstance()
    private val jugadoresRef = db.collection("jugadores")

    open fun insertarJugador(jugador: Jugador): Completable {
        return Completable.create { emitter ->
            jugadoresRef.document(jugador.uid)
                .set(jugador)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
        }
    }

    open fun obtenerJugador(nombre: String): Maybe<Jugador> {
        return Maybe.create { emitter ->
            jugadoresRef.document(nombre)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val jugador = doc.toObject(Jugador::class.java)
                        if (jugador != null) {
                            emitter.onSuccess(jugador)
                        } else {
                            emitter.onComplete()
                        }
                    } else {
                        emitter.onComplete()
                    }
                }
                .addOnFailureListener { emitter.onError(it) }
        }
    }

    open fun obtenerJugadorPorUid(uid: String): Maybe<Jugador> {
        return Maybe.create { emitter ->
            jugadoresRef.document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    val jugador = doc.toObject(Jugador::class.java)
                    if (jugador != null) emitter.onSuccess(jugador)
                    else emitter.onComplete()
                }
                .addOnFailureListener { emitter.onError(it) }
        }
    }

    open fun insertarJugadorConUid(nombre: String, uid: String): Completable {
        val jugador = hashMapOf(
            "nombre" to nombre,
            "uid" to uid,
            "monedas" to 100,
            "partidas" to 0,
            "victorias" to 0,
            "palo" to "Oros",
            "latitud" to null,
            "longitud" to null
        )
        return Completable.create { emitter ->
            jugadoresRef.document(nombre)
                .set(jugador)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
        }
    }


    open fun actualizarJugador(jugador: Jugador): Completable {
        return insertarJugador(jugador) // porque set() sobreescribe si existe
    }

    open fun actualizarUbicacion(nombre: String, lat: Double, lon: Double): Completable {
        return Completable.create { emitter ->
            jugadoresRef.document(nombre)
                .update(mapOf("latitud" to lat, "longitud" to lon))
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
        }
    }
}
