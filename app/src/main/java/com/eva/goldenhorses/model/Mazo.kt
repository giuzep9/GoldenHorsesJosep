package com.eva.goldenhorses.model

class Mazo {
    private val palos = listOf("Oros", "Copas", "Espadas", "Bastos")
    private val valores = listOf(1, 2, 3, 4, 5, 6, 7, 10, 12)

    private val cartas = mutableListOf<Carta>()
    private val descarte = mutableListOf<Carta>() // Aquí almacenamos las cartas jugadas

    init {
        generarBaraja()
        barajar()
    }

    // Generar baraja asegurando que no haya cartas previas
    private fun generarBaraja() {
        cartas.clear() // Evitar duplicados en caso de reiniciar
        for (palo in palos) {
            for (valor in valores) {
                cartas.add(Carta(palo, valor))
            }
        }
    }

    // Baraja el mazo de forma aleatoria
    fun barajar() {
        cartas.shuffle()
    }

    // Saca la primera carta del mazo y la elimina de la lista
    fun sacarCarta(): Carta? {
        if (cartas.isEmpty()) {
            if (descarte.isNotEmpty()) {
                reciclarMazo() // Recargar el mazo con las cartas usadas
            } else {
                return null // Si no hay cartas en el descarte, el mazo sigue vacío
            }
        }
        return cartas.removeAt(0)?.also { descarte.add(it) } // Agregar la carta al descarte
    }

    private fun reciclarMazo() {
        cartas.addAll(descarte) // Pasamos las cartas usadas al mazo
        descarte.clear() // Limpiamos el descarte
        barajar() // Mezclamos nuevamente
    }
}

