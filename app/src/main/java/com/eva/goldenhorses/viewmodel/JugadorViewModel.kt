package com.eva.goldenhorses.viewmodel

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.repository.JugadorRepository
import com.google.firebase.auth.FirebaseAuth
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

    private val _pais = MutableStateFlow<String?>(null)
    val pais: StateFlow<String?> = _pais

    // Inicia sesión y carga al jugador actual
    fun iniciarSesion(nombre: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val disposable = repository.obtenerJugador()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ jugadorExistente ->
                _jugador.value = jugadorExistente
            }, { error ->
                error.printStackTrace()
            }, {
                // Si no existe, lo creamos
                val nuevo = Jugador(nombre = nombre)
                repository.insertarJugador(nuevo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _jugador.value = nuevo
                    }, { it.printStackTrace() })
            })
        disposables.add(disposable)
    }

    // Comprueba si existe jugador y si no lo crea (por UID)
    fun comprobarOInsertarJugador(nombre: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val disposable = repository.obtenerJugador()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ jugadorExistente ->
                _jugador.value = jugadorExistente
            }, { error ->
                error.printStackTrace()
            }, {
                val nuevoJugador = Jugador(nombre = nombre)
                repository.insertarJugador(nuevoJugador)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _jugador.value = nuevoJugador
                    }, { it.printStackTrace() })
            })
        disposables.add(disposable)
    }

    fun insertarJugador(jugador: Jugador) {
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


    // Actualiza el jugador actual
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

    // Actualiza ubicación usando UID
    fun actualizarUbicacion(lat: Double, lon: Double) {
        val disposable = repository.actualizarUbicacion(lat, lon)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // Éxito
            }, {
                it.printStackTrace()
            })
        disposables.add(disposable)
    }

    // Actualiza el país (solo UI)
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
