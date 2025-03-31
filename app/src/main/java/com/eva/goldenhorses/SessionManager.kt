package com.eva.goldenhorses

import android.content.Context

object SessionManager {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_NOMBRE_JUGADOR = "jugador_nombre"

    fun guardarJugador(context: Context, nombre: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_NOMBRE_JUGADOR, nombre).apply()
    }

    fun obtenerJugador(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NOMBRE_JUGADOR, "Jugador") ?: "Jugador"
    }
}