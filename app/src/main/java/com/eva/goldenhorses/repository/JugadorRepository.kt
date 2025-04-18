package com.eva.goldenhorses.repository

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

