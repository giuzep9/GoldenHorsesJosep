package com.eva.goldenhorses.repository

import android.util.Log
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.model.JugadorRanking
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import java.text.SimpleDateFormat
import java.util.*

class JugadorRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    /** Inserta jugador en Realtime Database para que Retrofit lo pueda consumir */
    fun insertarJugadorEnRealtime(jugador: Jugador): Completable {
        return Completable.create { emitter ->
            val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val victoriasHoy = jugador.victoriasPorDia[fechaHoy] ?: 0

            val victoriaData = mapOf(
                "nombre" to jugador.nombre,
                "victoriasHoy" to victoriasHoy
            )

            val dbRealtime = FirebaseDatabase.getInstance().reference
            dbRealtime
                .child("ranking")
                .child(fechaHoy) // ahora se guarda por día
                .child(jugador.nombre)
                .setValue(victoriaData)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
        }
    }

    fun insertarJugador(jugador: Jugador): Completable {
        return Completable.create { emitter ->
            val jugadorMap = mapOf(
                "nombre" to jugador.nombre,
                "monedas" to jugador.monedas,
                "partidas" to jugador.partidas,
                "victorias" to jugador.victorias,
                "palo" to jugador.palo,
                "latitud" to jugador.latitud,
                "longitud" to jugador.longitud,
                "victoriasPorDia" to jugador.victoriasPorDia
            )

            db.collection("jugadores").document(jugador.nombre).set(jugadorMap)
                .addOnSuccessListener {
                    insertarJugadorEnRealtime(jugador)
                        .subscribe({ emitter.onComplete() }, { emitter.onError(it) })
                }
                .addOnFailureListener { emitter.onError(it) }
        }
    }

    fun obtenerJugador(nombre: String): Maybe<Jugador> {
        return Maybe.create { emitter ->
            db.collection("jugadores").document(nombre).get()
                .addOnSuccessListener { doc ->
                    doc.toObject(Jugador::class.java)?.let { emitter.onSuccess(it) } ?: emitter.onComplete()
                }
                .addOnFailureListener { emitter.onError(it) }
        }
    }

    fun actualizarJugador(jugador: Jugador): Completable {
        return insertarJugador(jugador) // Reutiliza el mismo método
    }

    fun actualizarUbicacion(nombre: String, lat: Double, lon: Double): Completable {
        return Completable.create { emitter ->
            db.collection("jugadores").document(nombre)
                .update(mapOf("latitud" to lat, "longitud" to lon))
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { emitter.onError(it) }
        }
    }

    fun actualizarMonedas(nombreJugador: String, cantidad: Int, onComplete: (Boolean) -> Unit) {
        db.collection("jugadores").whereEqualTo("nombre", nombreJugador).limit(1).get()
            .addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    val jugador = doc.toObject(Jugador::class.java)
                    val nuevasMonedas = (jugador?.monedas ?: 0) + cantidad
                    doc.reference.update("monedas", nuevasMonedas)
                        .addOnSuccessListener { onComplete(true) }
                        .addOnFailureListener { onComplete(false) }
                } else onComplete(false)
            }
            .addOnFailureListener { onComplete(false) }
    }

    /** Guarda una victoria con timestamp para cálculo diario */
    fun registrarVictoria(nombreJugador: String) {
        val jugadorRef = db.collection("jugadores").document(nombreJugador)
        val victoriaData = mapOf("fecha" to Timestamp.now())

        jugadorRef.collection("victorias").add(victoriaData)
            .addOnSuccessListener {
                Log.d("Firebase", "Victoria registrada para $nombreJugador")
            }
            .addOnFailureListener { Log.e("Firebase", "Error al registrar victoria", it) }
    }

    /** Cuenta las victorias del día de un jugador */
    fun contarVictoriasHoy(nombre: String, onResult: (Int) -> Unit) {
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        db.collection("jugadores").document(nombre)
            .collection("victorias")
            .whereGreaterThanOrEqualTo("fecha", Timestamp(hoy))
            .get()
            .addOnSuccessListener { snapshot -> onResult(snapshot.size()) }
            .addOnFailureListener { onResult(0) }
    }

    /** Genera ranking del día desde subcolección "victoriasPorDia" */
    fun obtenerRankingDelDia(callback: (List<JugadorRanking>) -> Unit) {
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val rankingList = mutableListOf<JugadorRanking>()

        db.collection("jugadores").get().addOnSuccessListener { snapshot ->
            var procesados = 0
            val total = snapshot.size()

            snapshot.forEach { jugadorDoc ->
                val nombre = jugadorDoc.id
                jugadorDoc.reference.collection("victoriasPorDia")
                    .document(fechaHoy)
                    .get()
                    .addOnSuccessListener { docDia ->
                        val victoriasHoy = docDia.getLong("victorias")?.toInt() ?: 0
                        if (victoriasHoy > 0) rankingList.add(JugadorRanking(nombre, victoriasHoy))
                        procesados++
                        if (procesados == total) callback(rankingList.sortedByDescending { it.victoriasHoy })
                    }
                    .addOnFailureListener {
                        procesados++
                        if (procesados == total) callback(rankingList.sortedByDescending { it.victoriasHoy })
                    }
            }
        }
    }

    /** Premia automáticamente con monedas si es el primer lugar del ranking */
    fun premiarSiEsPrimerLugar(ranking: List<JugadorRanking>, jugadorActual: String) {
        if (ranking.isNotEmpty() && ranking.first().nombre == jugadorActual) {
            actualizarMonedas(jugadorActual, 120) {
                if (it) Log.d("Premio", "120 monedas otorgadas a $jugadorActual")
            }
        }
    }
}




