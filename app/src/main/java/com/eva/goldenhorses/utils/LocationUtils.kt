package com.eva.goldenhorses.utils

import android.content.Context
import android.location.Geocoder
import java.util.*

fun obtenerPaisDesdeUbicacion(context: Context, latitud: Double?, longitud: Double?): String {
    return if (latitud != null && longitud != null) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val direcciones = geocoder.getFromLocation(latitud, longitud, 1)
            if (!direcciones.isNullOrEmpty()) {
                direcciones[0].countryName ?: "País desconocido"
            } else {
                "Ubicación desconocida"
            }
        } catch (e: Exception) {
            "Error al obtener ubicación"
        }
    } else {
        "Latitud o longitud no válida"
    }
}
