package com.eva.goldenhorses.model

data class Jugador(
    var nombre: String = "",          // Identificador (será el ID del documento)
    var monedas: Int = 20,            // Monedas iniciales
    var partidas: Int = 0,            // Número total de partidas
    var victorias: Int = 0,           // Número de partidas ganadas
    var palo: String = "Oros",        // Palo de la baraja
    var latitud: Double? = null,      // Latitud de la ubicación del jugador
    var longitud: Double? = null,     // Longitud de la ubicación del jugador
    var pais: String? = null          // Puedes añadir país si lo quieres guardar también
) {

    var apuesta: Apuesta? = null      // Campo que no se guarda en Firestore

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
