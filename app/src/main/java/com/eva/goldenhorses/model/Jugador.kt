package com.eva.goldenhorses.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Jugador(
    var nombre: String = "",
    var monedas: Int = 20,
    var partidas: Int = 0,
    var victorias: Int = 0, // ← victorias generales
    var palo: String = "Oros",
    var latitud: Double? = null,
    var longitud: Double? = null,
    var victoriasPorDia: Map<String, Int> = emptyMap(), // ← nuevas victorias diarias
    var premioReclamado: Map<String, Boolean> = emptyMap(), // ← control de premio

    @Transient var apuesta: Apuesta? = null
) {

    fun realizarApuesta(caballo: String) {
        if (monedas <= 0) {
            monedas += 20
        }

        monedas -= 20
        apuesta = Apuesta(caballo, 20)
    }

    fun actualizarMonedas(ganador: String) {
        if (palo == ganador) {
            monedas += 80
        }
        apuesta = null
    }
}
/*package com.eva.goldenhorses.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "jugadores")
data class Jugador(
    @PrimaryKey val nombre: String, // Clave primaria única (nombre del jugador)
    var monedas: Int = 20,         // Monedas iniciales
    var partidas: Int = 0,          // Número total de partidas
    var victorias: Int = 0,          // Número de partidas ganadas
    var palo: String = "Oros",
    var latitud: Double? = null,
    var longitud: Double? = null,

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
*/
