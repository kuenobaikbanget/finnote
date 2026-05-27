package com.app.finnote.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.finnote.data.AppContainer
import com.app.finnote.data.repository.FinNoteRepository
import com.app.finnote.data.repository.HomeSnapshot
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: FinNoteRepository
) : ViewModel() {
    private val _uiState = MutableLiveData<HomeSnapshot>()
    val uiState: LiveData<HomeSnapshot> = _uiState

    fun refresh() {
        val monthKey = repository.getCurrentMonthKey()
        viewModelScope.launch {
            _uiState.value = repository.getHomeSnapshot(monthKey)
        }
    }

    fun setMonthlyLimit(monthKey: String, limit: Int) {
        viewModelScope.launch {
            _uiState.value = repository.setMonthlyLimit(monthKey, limit)
        }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(AppContainer.repository) as T
            }
        }
    }
}
