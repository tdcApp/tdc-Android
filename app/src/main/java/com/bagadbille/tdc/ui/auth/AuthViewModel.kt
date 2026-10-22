package com.bagadbille.tdc.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.UserProfile
import com.bagadbille.tdc.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val user: UserProfile) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: com.bagadbille.tdc.data.repository.ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    val isLoggedIn: Flow<Boolean> = authRepository.isLoggedIn()
    val isProfileComplete: Flow<Boolean> = authRepository.isProfileComplete()
    val userRole: Flow<String?> = authRepository.getUserRole()
    val hasCompletedOnboarding: Flow<Boolean> = authRepository.hasCompletedOnboarding()

    fun completeOnboarding() {
        viewModelScope.launch {
            authRepository.setCompletedOnboarding(true)
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signInWithEmail(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure {
                    _uiState.value = AuthUiState.Error(it.message ?: "You're not a part of TDC")
                }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password are required")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signUpWithEmail(email, password)
                .onSuccess { _uiState.value = AuthUiState.Success(it) }
                .onFailure { _uiState.value = AuthUiState.Error(it.message ?: "Registration failed") }
        }
    }

    fun resetState() { _uiState.value = AuthUiState.Idle }

    suspend fun verifyProfile(): Boolean {
        return try {
            val result = profileRepository.getProfile()
            if (result.isSuccess) {
                val user = result.getOrNull()
                user != null && (com.bagadbille.tdc.data.model.isMentorRole(user.role) || user.isProfileComplete)
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
