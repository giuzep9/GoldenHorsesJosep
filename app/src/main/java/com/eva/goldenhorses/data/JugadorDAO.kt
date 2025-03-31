package com.eva.goldenhorses.data

import androidx.room.*
import com.eva.goldenhorses.model.Jugador

@Dao
interface JugadorDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarJugador(jugador: Jugador)

    @Query("SELECT * FROM jugadores WHERE nombre = :nombre LIMIT 1")
    suspend fun obtenerJugador(nombre: String): Jugador?

    @Update
    suspend fun actualizarJugador(jugador: Jugador)
}
