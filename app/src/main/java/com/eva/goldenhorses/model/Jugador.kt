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
