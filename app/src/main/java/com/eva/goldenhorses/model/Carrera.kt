package com.eva.goldenhorses.model

class Carrera {
    private val caballos = mutableListOf(
        Caballo("Oros"),
        Caballo("Copas"),
        Caballo("Espadas"),
        Caballo("Bastos")
    )

    private val mazo = Mazo()
    private val cartasRetroceso = mutableListOf<Carta>()
    private var ganador: Caballo? = null
    private val nivelMax = 8

    init {
        generarCartasRetroceso()
    }

    fun generarCartasRetroceso() {
        mazo.barajar()
        cartasRetroceso.clear()

        repeat(7) {
            val carta = mazo.sacarCarta()
            if (carta != null) {
                cartasRetroceso.add(carta)
                println("Carta de retroceso añadida: ${carta.palo} - ${carta.valor}")
            }
        }
        println("Cartas de retroceso generadas: $cartasRetroceso")
    }

    fun moverCaballo(palo: String) {
        if (ganador != null) return // Si ya hay ganador, no se mueve nada

        val caballo = caballos.find { it.palo == palo }
        caballo?.avanzar()
        actualizarCaballoUI()

        if (caballo?.haCruzadoMeta(nivelMax) == true && ganador == null) {
            ganador = caballo
        }
    }


    fun todosCaballosAlNivel(nivel: Int): Boolean {
        caballos.forEach { println("Caballo ${it.palo}: Posición ${it.posicion}") }
        return caballos.all { it.posicion >= nivel }
    }

    fun obtenerEstadoCarrera(): List<Caballo> {
        return caballos
    }

    fun retrocederCaballo(palo: String) {
        val caballo = caballos.find { it.palo == palo }
        if (caballo != null && caballo.posicion > 0) {
            caballo.retroceder()
            actualizarCaballoUI()
        }
    }

    fun obtenerGanador(): Caballo? {
        return ganador
    }

    fun obtenerCartasRetroceso(): List<Carta> {
        return cartasRetroceso
    }

    fun esCarreraFinalizada(): Boolean {
        return ganador != null
    }


    fun sacarCarta(): Carta? {
        return mazo.sacarCarta()?.also {
            println("Carta extraída: ${it.palo} - ${it.valor}")
        }
    }

    fun actualizarCaballoUI() {
        caballos.forEach { println(" ${it.palo} en posición ${it.posicion}") }
    }


}
