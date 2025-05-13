package com.eva.goldenhorses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eva.goldenhorses.model.JugadorRanking
import com.eva.goldenhorses.network.RetrofitService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RankingViewModel : ViewModel() {

    private val _ranking = MutableStateFlow<List<JugadorRanking>>(emptyList())
    val ranking: StateFlow<List<JugadorRanking>> = _ranking

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarRankingDeHoy()
    }

    private fun cargarRankingDeHoy() {
        viewModelScope.launch {
            try {
                val fechaHoy = obtenerFechaHoy()
                val response = RetrofitService.api.getRankingPorFecha(fechaHoy)
                val lista = response.values.sortedByDescending { it.victoriasHoy }
                _ranking.value = lista
            } catch (e: Exception) {
                _error.value = "Error al cargar el ranking: ${e.localizedMessage}"
            }
        }
    }

    private fun obtenerFechaHoy(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}


