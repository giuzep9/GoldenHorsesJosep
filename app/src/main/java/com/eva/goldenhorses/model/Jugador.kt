package com.eva.goldenhorses.model

data class Jugador(
    var uid: String = "",              // UID de Firebase Auth, ahora es el ID del documento
    var nombre: String = "",           // Nombre elegido por el usuario
    var monedas: Int = 100,
    var partidas: Int = 0,
    var victorias: Int = 0,
    var palo: String = "Oros",
    var latitud: Double? = null,
    var longitud: Double? = null,
    var pais: String? = null
) {
    var apuesta: Apuesta? = null  // No se guarda en Firestore

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
