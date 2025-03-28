package com.eva.goldenhorses.model

data class Jugador(
    val nombre: String,
    val palo: String,
    var monedas: Int,
    var apuesta: Apuesta? = null
) {
    fun realizarApuesta(caballo: String) {
        if (monedas <= 0) {
            // Se le dan 20 monedas si no tiene nada
            monedas += 20
        }

        monedas -= 20
        apuesta = Apuesta(caballo, 20)
    }

    fun actualizarMonedas(ganador: String) {
        apuesta?.let {
            if (it.caballo == ganador) {
                monedas += 80 // Gana 20x4
            }
            apuesta = null
        }
    }
}
