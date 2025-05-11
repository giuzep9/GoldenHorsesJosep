package com.eva.goldenhorses.repository

import android.util.Log
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.model.JugadorRanking
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    fun registrarVictoria(nombreJugador: String) {
        val jugadorRef = db.collection("jugadores").document(nombreJugador)
        val victoriaData = mapOf(
            "fecha" to Timestamp.now() // Guardamos el timestamp de la victoria
        )

        // Añadimos una victoria al jugador
        jugadorRef.collection("victorias")
            .add(victoriaData)
            .addOnSuccessListener {
                Log.d("Firebase", "Victoria registrada para $nombreJugador")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al registrar victoria", e)
            }
    }
    fun contarVictoriasHoy(nombre: String, onResult: (Int) -> Unit) {
        val jugadorRef = db.collection("jugadores").document(nombre)
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // Contamos las victorias del día para ese jugador
        jugadorRef.collection("victorias")
            .whereGreaterThanOrEqualTo("fecha", Timestamp(hoy))
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.size()) // El tamaño del snapshot nos da la cantidad de victorias
            }
            .addOnFailureListener {
                onResult(0) // Si hay error, devolvemos 0 victorias
            }
    }
    fun obtenerRankingDelDia(callback: (List<JugadorRanking>) -> Unit) {
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val db = FirebaseFirestore.getInstance()
        val rankingList = mutableListOf<JugadorRanking>()

        db.collection("jugadores").get().addOnSuccessListener { jugadoresSnapshot ->
            val total = jugadoresSnapshot.size()
            var procesados = 0

            for (jugadorDoc in jugadoresSnapshot) {
                val nombre = jugadorDoc.id

                jugadorDoc.reference.collection("victoriasPorDia")
                    .document(fechaHoy)
                    .get()
                    .addOnSuccessListener { docDia ->
                        val victoriasHoy = docDia.getLong("victorias")?.toInt() ?: 0
                        if (victoriasHoy > 0) {
                            rankingList.add(JugadorRanking(nombre, victoriasHoy))
                        }

                        procesados++
                        if (procesados == total) {
                            // Ordenar por victorias descendente
                            callback(rankingList.sortedByDescending { it.victoriasHoy })
                        }
                    }
            }
        }
    }

    /*fun obtenerRankingDelDia(onResult: (List<JugadorRanking>) -> Unit) {
        val jugadoresRef = db.collection("jugadores")

        jugadoresRef.get()
            .addOnSuccessListener { snapshot ->
                val listaRanking = mutableListOf<JugadorRanking>()

                // Contamos las victorias de cada jugador
                snapshot.documents.forEach { document ->
                    val nombre = document.getString("nombre") ?: return@forEach
                    contarVictoriasHoy(nombre) { victoriasHoy ->
                        listaRanking.add(JugadorRanking(nombre, victoriasHoy))

                        // Cuando hemos contado las victorias de todos, ordenamos el ranking
                        if (listaRanking.size == snapshot.size()) {
                            listaRanking.sortByDescending { it.victoriasHoy }
                            onResult(listaRanking.take(10))  // Devolvemos los 10 primeros
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al obtener ranking", e)
            }
    }*/
    fun premiarSiEsPrimerLugar(ranking: List<JugadorRanking>, jugadorActual: String) {
        if (ranking.isNotEmpty() && ranking[0].nombre == jugadorActual) {
            // Premiar con 120 monedas
            actualizarMonedas(jugadorActual, 120) { exito ->
                if (exito) {
                    Log.d("Premio", "¡Felicidades! Has ganado 120 monedas.")
                }
            }
        }
    }

    fun actualizarMonedas(nombreJugador: String, cantidad: Int, onComplete: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val jugadoresRef = db.collection("jugadores")

        jugadoresRef.whereEqualTo("nombre", nombreJugador)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val documento = querySnapshot.documents.firstOrNull()
                if (documento != null) {
                    val jugador = documento.toObject(Jugador::class.java)
                    val nuevasMonedas = (jugador?.monedas ?: 0) + cantidad
                    documento.reference.update("monedas", nuevasMonedas)
                        .addOnSuccessListener { onComplete(true) }
                        .addOnFailureListener { onComplete(false) }
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

}



