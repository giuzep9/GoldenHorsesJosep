//package com.eva.goldenhorses.data

//import androidx.room.*
//import com.eva.goldenhorses.model.Jugador
//import io.reactivex.rxjava3.core.Completable
//import io.reactivex.rxjava3.core.Maybe

//@Dao
//interface JugadorDAO {

//@Insert(onConflict = OnConflictStrategy.REPLACE)
//fun insertarJugador(jugador: Jugador): Completable

//@Query("SELECT * FROM jugadores WHERE nombre = :nombre LIMIT 1")
//fun obtenerJugador(nombre: String): Maybe<Jugador>

//@Update
//fun actualizarJugador(jugador: Jugador): Completable

//@Query("UPDATE jugadores SET latitud = :lat, longitud = :lon WHERE nombre = :nombre")
//fun actualizarUbicacion(nombre: String, lat: Double, lon: Double): Completable
//}