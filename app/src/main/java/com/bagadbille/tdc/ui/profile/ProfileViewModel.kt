package com.bagadbille.tdc.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.model.UserProfile
import com.bagadbille.tdc.data.repository.AuthRepository
import com.bagadbille.tdc.data.repository.ClassRepository
import com.bagadbille.tdc.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiData(
    val profile: UserProfile,
    val languageClass: ClassInfo? = null,
    val technologyClass: ClassInfo? = null,
    val isUpdating: Boolean = false
)

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val data: ProfileUiData) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ProfileEvent {
    data class ShowMessage(val message: String) : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val classRepository: ClassRepository,
    private val authRepository: AuthRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val isDarkTheme: StateFlow<Boolean> = dataStoreManager.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _eventFlow = MutableSharedFlow<ProfileEvent>()
    val eventFlow: SharedFlow<ProfileEvent> = _eventFlow.asSharedFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            val profileResult = profileRepository.getProfile()
            if (profileResult.isFailure) {
                _uiState.value = ProfileUiState.Error(
                    profileResult.exceptionOrNull()?.message ?: "Failed to load profile"
                )
                return@launch
            }

            val profile = profileResult.getOrNull()!!
            val classesResult = classRepository.getClasses()
            val classes = classesResult.getOrDefault(emptyList())

            val langClass = classes.find { it.id == profile.languageClassId }
            val techClass = classes.find { it.id == profile.technologyClassId }

            _uiState.value = ProfileUiState.Success(
                ProfileUiData(
                    profile = profile,
                    languageClass = langClass,
                    technologyClass = techClass
                )
            )
        }
    }

    fun toggleDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setDarkTheme(isDark)
        }
    }

    fun updateProfile(name: String, phone: String, onFinished: (Boolean) -> Unit = {}) {
        val currentState = _uiState.value
        if (currentState !is ProfileUiState.Success) return

        viewModelScope.launch {
            _uiState.value = ProfileUiState.Success(
                currentState.data.copy(isUpdating = true)
            )

            val result = profileRepository.updateProfile(name = name.trim(), phone = phone.trim())
            result.onSuccess { updatedProfile ->
                _uiState.value = ProfileUiState.Success(
                    currentState.data.copy(
                        profile = updatedProfile,
                        isUpdating = false
                    )
                )
                _eventFlow.emit(ProfileEvent.ShowMessage("Profile updated successfully"))
                onFinished(true)
            }.onFailure { error ->
                _uiState.value = ProfileUiState.Success(
                    currentState.data.copy(isUpdating = false)
                )
                val msg = error.message ?: "Failed to update profile"
                _eventFlow.emit(ProfileEvent.ShowMessage(msg))
                onFinished(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
