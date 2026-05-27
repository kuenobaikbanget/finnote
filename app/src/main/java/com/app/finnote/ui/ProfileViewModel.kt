package com.app.finnote.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.finnote.data.AppContainer
import com.app.finnote.data.repository.FinNoteRepository
import com.app.finnote.data.repository.ProfileSnapshot
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: FinNoteRepository
) : ViewModel() {
    private val _profile = MutableLiveData<ProfileSnapshot>()
    val profile: LiveData<ProfileSnapshot> = _profile

    fun refresh() {
        viewModelScope.launch {
            _profile.value = repository.getProfileSnapshot()
        }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(AppContainer.repository) as T
            }
        }
    }
}
