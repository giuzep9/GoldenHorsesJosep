package com.eva.goldenhorses.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.activity.ComponentActivity
import java.util.*

fun cambiarIdioma(context: Context, idioma: String) {
    val locale = Locale(idioma)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocale(locale)
    } else {
        config.locale = locale
    }

    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

fun aplicarIdioma(context: Context): Context {
    val idioma = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getString("idioma", "es") ?: "es"

    val locale = Locale(idioma)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocale(locale)
        context.createConfigurationContext(config)
    } else {
        config.locale = locale
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        context
    }
}

fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    context.startActivity(intent)
    if (context is ComponentActivity) {
        context.finish()
    }
}

fun guardarIdioma(context: Context, idioma: String) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("idioma", idioma).apply()
}

fun obtenerIdioma(context: Context): String {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getString("idioma", "es") ?: "es"
}