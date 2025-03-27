package com.eva.goldenhorses.model

data class Jugador(
    val nombre: String,
    val palo: String,
    var monedas: Int,
    var apuesta: Apuesta? = null
) {
    fun realizarApuesta(caballo: String, cantidad: Int) {
        if (cantidad <= monedas) {
            monedas -= cantidad
            apuesta = Apuesta(caballo, cantidad)
        }
    }

    fun actualizarMonedas(ganador: String) {
        apuesta?.let {
            monedas += it.calcularGanancia(ganador)
            apuesta = null
        }
    }
}
