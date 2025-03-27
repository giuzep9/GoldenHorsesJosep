package com.eva.goldenhorses.model

data class Caballo(val palo: String, var posicion: Int = 0) {

    fun avanzar() {
        posicion++
    }

    fun retroceder() {
        if (posicion > 0) {
            posicion--
        }
    }

    fun haCruzadoMeta(nivelMax: Int): Boolean {
        return posicion >= nivelMax
    }
}
