package com.eva.goldenhorses.repository

import android.util.Log
import com.eva.goldenhorses.data.JugadorDAO
import com.eva.goldenhorses.model.Jugador

class JugadorRepository(private val jugadorDAO: JugadorDAO) {
    suspend fun insertarJugador(jugador: Jugador) = jugadorDAO.insertarJugador(jugador)
    suspend fun obtenerJugador(nombre: String): Jugador? = jugadorDAO.obtenerJugador(nombre)
    suspend fun actualizarJugador(jugador: Jugador) {
        jugadorDAO.actualizarJugador(jugador)
        Log.d(
            "DB_UPDATE",
            "Jugador actualizado: ${jugador.nombre} | Partidas: ${jugador.partidas} | Victorias: ${jugador.victorias}"
        )
    }
}
