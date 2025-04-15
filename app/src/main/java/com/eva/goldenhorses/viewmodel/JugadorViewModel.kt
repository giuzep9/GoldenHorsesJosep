package com.eva.goldenhorses.viewmodel

import androidx.lifecycle.ViewModel
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.repository.JugadorRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class JugadorViewModel(val repository: JugadorRepository) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val _jugador = MutableStateFlow<Jugador?>(null)
    val jugador: StateFlow<Jugador?> = _jugador

    fun iniciarSesion(nombre: String) {
        val disposable = repository.obtenerJugador(nombre)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ jugadorExistente ->
                _jugador.value = jugadorExistente
            }, { error ->
                error.printStackTrace()
            }, {
                // No existe → crear uno nuevo
                val nuevo = Jugador(nombre, 100, 0, 0, "Oros")
                insertarJugador(nuevo)
                _jugador.value = nuevo
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
                // Insertado con éxito
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
                // Ya existe → nada que hacer
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

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
