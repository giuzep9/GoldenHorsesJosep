package com.eva.goldenhorses.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "jugadores")
data class Jugador(
    @PrimaryKey val nombre: String,  // Clave primaria única (nombre del jugador)
    var monedas: Int = 20,           // Monedas iniciales
    var partidas: Int = 0,           // Número total de partidas
    var victorias: Int = 0,          // Número de partidas ganadas
    var palo: String = "Oros",       // Palo de la baraja
    var latitud: Double? = null,     // Latitud de la ubicación del jugador
    var longitud: Double? = null,    // Longitud de la ubicación del jugador

    @Ignore var apuesta: Apuesta? = null // Igual, solo aplica a lógica temporal
) {
    constructor(nombre: String, monedas: Int, partidas: Int, victorias: Int) : this(
        nombre,
        monedas,
        partidas,
        victorias,
        "Oros", // valor por defecto para que Room lo acepte
        null,
        null
    )

    fun realizarApuesta(caballo: String) {
        if (monedas <= 0) {
            // Se le dan 20 monedas si no tiene nada
            monedas += 20
        }

        monedas -= 20
        apuesta = Apuesta(caballo, 20)
    }

    fun actualizarMonedas(ganador: String) {
        if (palo == ganador) {
            monedas += 80 // Gana 20x4
        }
        apuesta = null
    }
}

