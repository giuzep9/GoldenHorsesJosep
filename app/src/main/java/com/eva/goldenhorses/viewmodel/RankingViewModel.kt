package com.eva.goldenhorses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eva.goldenhorses.model.JugadorRanking
import com.eva.goldenhorses.network.ApiClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RankingViewModel : ViewModel() {

    private val _ranking = MutableStateFlow<List<JugadorRanking>>(emptyList())
    val ranking: StateFlow<List<JugadorRanking>> = _ranking

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarRanking()
    }

    fun cargarRanking() {
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario == null) {
            _error.value = "Usuario no autenticado"
            return
        }

        usuario.getIdToken(true)
            .addOnSuccessListener { result ->
                val token = result.token ?: ""
                viewModelScope.launch {
                    try {
                        val resultado = ApiClient.rankingApi.obtenerRanking(token)
                        _ranking.value = resultado.values.sortedByDescending { it.victoriasHoy }

                        _error.value = null
                    } catch (e: Exception) {
                        _error.value = "Error al cargar ranking: ${e.message}"
                    }
                }
            }
            .addOnFailureListener {
                _error.value = "Error al obtener token de Firebase"
            }
    }
}


/*package com.eva.goldenhorses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eva.goldenhorses.model.JugadorRanking
import com.eva.goldenhorses.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RankingViewModel : ViewModel() {

    private val _ranking = MutableStateFlow<List<JugadorRanking>>(emptyList())
    val ranking: StateFlow<List<JugadorRanking>> = _ranking

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarRanking()
    }

    fun cargarRanking() {
        viewModelScope.launch {
            try {
                val resultado = ApiClient.rankingApi.obtenerRanking()
                _ranking.value = resultado.sortedByDescending { it.victoriasHoy }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cargar ranking: ${e.message}"
            }
        }
    }
}*/
