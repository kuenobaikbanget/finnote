package com.app.finnote.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.finnote.data.AppContainer
import com.app.finnote.data.repository.FinNoteRepository
import com.app.finnote.model.Transaction
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val repository: FinNoteRepository
) : ViewModel() {
    private val _transaction = MutableLiveData<Transaction?>()
    val transaction: LiveData<Transaction?> = _transaction

    fun load(index: Int) {
        viewModelScope.launch {
            _transaction.value = repository.getTransaction(index)
        }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TransactionDetailViewModel(AppContainer.repository) as T
            }
        }
    }
}
