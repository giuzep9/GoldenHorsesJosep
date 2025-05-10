package com.eva.goldenhorses.repository

import com.eva.goldenhorses.model.Jugador
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

class JugadorRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun insertarJugador(jugador: Jugador): Completable {
        return Completable.create { emitter ->
            val jugadorMap = mapOf(
                "nombre" to jugador.nombre,
                "monedas" to jugador.monedas,
                "partidas" to jugador.partidas,
                "victorias" to jugador.victorias,
                "palo" to jugador.palo,
                "latitud" to jugador.latitud,
                "longitud" to jugador.longitud
                // NO incluimos "apuesta"
            )

            db.collection("jugadores")
                .document(jugador.nombre)
                .set(jugadorMap)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { e -> emitter.onError(e) }
        }
    }

    fun obtenerJugador(nombre: String): Maybe<Jugador> {
        return Maybe.create { emitter ->
            db.collection("jugadores")
                .document(nombre)
                .get()
                .addOnSuccessListener { doc ->
                    doc.toObject(Jugador::class.java)?.let {
                        emitter.onSuccess(it)
                    } ?: emitter.onComplete()
                }
                .addOnFailureListener { e -> emitter.onError(e) }
        }
    }

    fun actualizarJugador(jugador: Jugador): Completable {
        return insertarJugador(jugador) // Reutilizamos el mismo método
    }

    fun actualizarUbicacion(nombre: String, lat: Double, lon: Double): Completable {
        return Completable.create { emitter ->
            db.collection("jugadores")
                .document(nombre)
                .update(mapOf("latitud" to lat, "longitud" to lon))
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { e -> emitter.onError(e) }
        }
    }
}

/*package com.eva.goldenhorses.repository

import com.eva.goldenhorses.model.Jugador
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

class JugadorRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun insertarJugador(jugador: Jugador): Completable {
        return Completable.create { emitter ->
            db.collection("jugadores")
                .document(jugador.nombre)
                .set(jugador)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { e -> emitter.onError(e) }
        }
    }

    fun obtenerJugador(nombre: String): Maybe<Jugador> {
        return Maybe.create { emitter ->
            db.collection("jugadores")
                .document(nombre)
                .get()
                .addOnSuccessListener { doc ->
                    doc.toObject(Jugador::class.java)?.let {
                        emitter.onSuccess(it)
                    } ?: emitter.onComplete()
                }
                .addOnFailureListener { e -> emitter.onError(e) }
        }
    }

    fun actualizarJugador(jugador: Jugador): Completable {
        return insertarJugador(jugador) // Reutilizamos el set para actualizar
    }

    fun actualizarUbicacion(nombre: String, lat: Double, lon: Double): Completable {
        return Completable.create { emitter ->
            db.collection("jugadores")
                .document(nombre)
                .update(mapOf("latitud" to lat, "longitud" to lon))
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { e -> emitter.onError(e) }
        }
    }
}*/
/*package com.eva.goldenhorses.repository

import com.eva.goldenhorses.data.JugadorDAO
import com.eva.goldenhorses.model.Jugador

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

class JugadorRepository(private val jugadorDAO: JugadorDAO) {

    fun insertarJugador(jugador: Jugador): Completable =
        jugadorDAO.insertarJugador(jugador)

    fun obtenerJugador(nombre: String): Maybe<Jugador> =
        jugadorDAO.obtenerJugador(nombre)

    fun actualizarJugador(jugador: Jugador): Completable =
        jugadorDAO.actualizarJugador(jugador)

    fun actualizarUbicacion(nombre: String, lat: Double, lon: Double): Completable =
        jugadorDAO.actualizarUbicacion(nombre, lat, lon)
}

*/