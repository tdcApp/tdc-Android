package com.bagadbille.tdc.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.UserProfile
import com.bagadbille.tdc.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SetupUiState {
    data object Idle : SetupUiState()
    data object Loading : SetupUiState()
    data class Success(val user: UserProfile) : SetupUiState()
    data class Error(val message: String) : SetupUiState()
}

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<SetupUiState>(SetupUiState.Idle)
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun setupProfile(name: String, enrollmentNumber: String, year: String) {
        if (name.isBlank()) { _uiState.value = SetupUiState.Error("Name is required"); return }
        if (enrollmentNumber.isBlank()) { _uiState.value = SetupUiState.Error("Enrollment number is required"); return }
        if (year.isBlank()) { _uiState.value = SetupUiState.Error("Please select your year"); return }

        viewModelScope.launch {
            _uiState.value = SetupUiState.Loading
            profileRepository.setupProfile(name, enrollmentNumber, year)
                .onSuccess { _uiState.value = SetupUiState.Success(it) }
                .onFailure { _uiState.value = SetupUiState.Error(it.message ?: "Setup failed") }
        }
    }
}
