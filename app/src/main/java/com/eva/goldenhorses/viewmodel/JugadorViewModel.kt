package com.eva.goldenhorses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eva.goldenhorses.model.Jugador
import com.eva.goldenhorses.repository.JugadorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JugadorViewModel(private val repository: JugadorRepository) : ViewModel() {
    private val _jugador = MutableStateFlow<Jugador?>(null)
    val jugador: StateFlow<Jugador?> = _jugador

    fun iniciarSesion(nombre: String) {
        viewModelScope.launch {
            val jugadorExistente = repository.obtenerJugador(nombre)
            if (jugadorExistente != null) {
                _jugador.value = jugadorExistente
            } else {
                val nuevo = Jugador(nombre = nombre, monedas = 100, partidas = 0, victorias = 0)
                repository.insertarJugador(nuevo)
                _jugador.value = nuevo
            }
        }
    }

    fun comprobarOInsertarJugador(nombre: String) {
        viewModelScope.launch {
            val jugadorExistente = repository.obtenerJugador(nombre)
            if (jugadorExistente == null) {
                val nuevoJugador = Jugador(nombre = nombre, monedas = 100, partidas = 0, victorias = 0)
                repository.insertarJugador(nuevoJugador)
            }
            // Si ya existe, no hace falta hacer nada. Se usará más adelante en HomeActivity.
        }
    }

    suspend fun obtenerJugador(nombre: String): Jugador? {
        return repository.obtenerJugador(nombre)
    }


}
