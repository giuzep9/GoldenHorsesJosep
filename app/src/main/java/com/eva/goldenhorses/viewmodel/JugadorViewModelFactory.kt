package com.eva.goldenhorses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eva.goldenhorses.repository.JugadorRepository

class JugadorViewModelFactory(private val repository: JugadorRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return JugadorViewModel(repository) as T
    }
}
