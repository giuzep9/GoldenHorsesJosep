package com.eva.goldenhorses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eva.goldenhorses.model.JugadorRanking
import com.eva.goldenhorses.network.ApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    fun guardarGanadorDelDiaAnterior() {
        val db = FirebaseFirestore.getInstance()
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fechaAyer = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.time)

        val ganadorDelDiaAnterior = ranking.value.maxByOrNull { it.victoriasHoy }
        if (ganadorDelDiaAnterior != null) {
            // Guardar al ganador
            val ganadorRef = db.collection("ranking_del_dia_anterior").document(fechaAyer)
            ganadorRef.set(mapOf("ganador" to ganadorDelDiaAnterior.nombre, "fecha" to fechaAyer))
                .addOnSuccessListener {
                    // Guardado exitoso
                }
                .addOnFailureListener {
                    // Error al guardar
                }
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
