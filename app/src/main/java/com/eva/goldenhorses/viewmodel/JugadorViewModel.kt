package com.eva.goldenhorses.viewmodel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.repository.JugadorRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class JugadorViewModel(val repository: JugadorRepository) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val _jugador = MutableStateFlow<Jugador?>(null)
    val jugador: StateFlow<Jugador?> = _jugador
    private val _nombreUsuarioExiste = MutableStateFlow<Boolean?>(null)
    val nombreUsuarioExiste: StateFlow<Boolean?> = _nombreUsuarioExiste
    private val _pais = MutableStateFlow<String?>(null)
    val pais: StateFlow<String?> = _pais

    fun comprobarJugadorPorUid(uid: String) {
        val disposable = repository.obtenerJugadorPorUid(uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ jugador ->
                _jugador.value = jugador
                _nombreUsuarioExiste.value = true
            }, {
                it.printStackTrace()
            }, {
                _nombreUsuarioExiste.value = false  // No existe, ir a LoginActivity
            })
        disposables.add(disposable)
    }

    fun actualizarJugador(jugador: Jugador) {
        val disposable = repository.actualizarJugador(jugador)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _jugador.value = jugador
            }, {
                it.printStackTrace()
            })
        disposables.add(disposable)
    }

    fun insertarJugador(jugador: Jugador) {
        val disposable = repository.insertarJugador(jugador)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _jugador.value = jugador  // ← lo marcamos como jugador actual
            }, {
                it.printStackTrace()
            })
        disposables.add(disposable)
    }

    fun crearJugador(uid: String, nombre: String) {
        val jugador = Jugador(uid = uid, nombre = nombre)
        val disposable = repository.insertarJugador(jugador)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _jugador.value = jugador
            }, {
                it.printStackTrace()
            })
        disposables.add(disposable)
    }

    fun comprobarOInsertarJugador(nombre: String) {
        val disposable = repository.obtenerJugador(nombre)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ jugador ->
                _jugador.value = jugador  // Si ya existe, lo marcamos
            }, {
                it.printStackTrace()
            }, {
                // No existe → insertamos
                val nuevoJugador = Jugador(nombre = nombre, monedas = 100, partidas = 0, victorias = 0)
                insertarJugador(nuevoJugador)
            })
        disposables.add(disposable)
    }

    fun actualizarUbicacion(nombre: String, lat: Double, lon: Double) {
        val disposable = repository.actualizarUbicacion(nombre, lat, lon)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // Actualización exitosa
            }, {
                it.printStackTrace()
            })
        disposables.add(disposable)
    }

    fun actualizarPaisDesdeUbicacion(context: Context, lat: Double?, lon: Double?) {
        if (lat != null && lon != null) {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val country = addresses?.firstOrNull()?.countryName ?: "Desconocido"
            _pais.value = country
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
