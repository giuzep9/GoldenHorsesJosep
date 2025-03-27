package com.eva.goldenhorses.model

data class Apuesta(
    val caballo: String, // Palo del caballo
    var cantidad: Int
) {
    fun calcularGanancia(ganador: String): Int {
        return if (caballo == ganador) cantidad * 2 else 0
    }
}
