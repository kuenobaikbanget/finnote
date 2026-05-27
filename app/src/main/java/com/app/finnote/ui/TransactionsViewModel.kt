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

class TransactionsViewModel(
    private val repository: FinNoteRepository
) : ViewModel() {
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    fun refresh() {
        viewModelScope.launch {
            _transactions.value = repository.getTransactions()
        }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TransactionsViewModel(AppContainer.repository) as T
            }
        }
    }
}
