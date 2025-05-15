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

    // Esta función inicia sesión, obteniendo el jugador o creándolo si no existe
    fun iniciarSesion(nombre: String) {
        val disposable = repository.obtenerJugador()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ jugadorExistente ->
                _jugador.value = jugadorExistente
            }, { error ->
                error.printStackTrace()
            }, {
                // Si no existe, lo creamos con el UID
                val nuevo = Jugador(nombre = nombre)
                insertarJugador(nuevo)
                _jugador.value = nuevo
            })
        disposables.add(disposable)
    }

    fun iniciarSesionConUID(uid: String) {
        val disposable = repository.obtenerJugadorPorUID(uid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ jugadorExistente ->
                _jugador.value = jugadorExistente
            }, { error ->
                error.printStackTrace()
            }, {
                // Si no existe, creamos uno nuevo
                val nuevo = Jugador(nombre = FirebaseAuth.getInstance().currentUser?.displayName ?: "Anónimo")
                repository.insertarJugadorConUID(uid, nuevo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _jugador.value = nuevo
                    }, { it.printStackTrace() })
            })
        disposables.add(disposable)
    }

    // Esta función actualiza los datos del jugador
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

    // Esta función inserta un jugador nuevo
    fun insertarJugador(jugador: Jugador) {
        val disposable = repository.insertarJugador(jugador)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // El jugador se inserta correctamente
            }, {
                it.printStackTrace()
            })
        disposables.add(disposable)
    }

    // Esta función comprueba si un jugador existe y si no, lo inserta
    fun comprobarOInsertarJugador(nombre: String) {
        val disposable = repository.obtenerJugador()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ jugador ->
                // Ya existe, no hacer nada
            }, {
                it.printStackTrace()
            }, {
                val nuevoJugador = Jugador(nombre = nombre)
                insertarJugador(nuevoJugador)
            })
        disposables.add(disposable)
    }

    // Actualiza la ubicación del jugador en Firebase
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

    // Esta función actualiza el país basado en la latitud y longitud
    fun actualizarPaisDesdeUbicacion(context: Context, lat: Double?, lon: Double?) {
        if (lat != null && lon != null) {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val country = addresses?.firstOrNull()?.countryName ?: "Desconocido"
            _pais.value = country
        }
    }

    // Limpiar los recursos cuando el ViewModel es destruido
    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
