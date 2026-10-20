package com.bagadbille.tdc.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.model.UserProfile
import com.bagadbille.tdc.data.repository.ClassRepository
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
    private val profileRepository: ProfileRepository,
    private val classRepository: ClassRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<SetupUiState>(SetupUiState.Idle)
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    private val _languageClasses = MutableStateFlow<List<ClassInfo>>(emptyList())
    val languageClasses: StateFlow<List<ClassInfo>> = _languageClasses.asStateFlow()

    private val _technologyClasses = MutableStateFlow<List<ClassInfo>>(emptyList())
    val technologyClasses: StateFlow<List<ClassInfo>> = _technologyClasses.asStateFlow()

    init {
        loadClasses()
    }

    private fun loadClasses() {
        viewModelScope.launch {
            val classes = classRepository.getClasses().getOrDefault(emptyList())
            _languageClasses.value = classes.filter { it.type.equals("language", ignoreCase = true) }
            _technologyClasses.value = classes.filter { it.type.equals("technology", ignoreCase = true) }
        }
    }

    fun completeProfile(
        name: String,
        languageClassId: String?,
        technologyClassId: String?,
        enrollmentNumber: String? = null,
        year: String? = null,
        mobileNumber: String? = null
    ) {
        val cleanName = name.trim()
        val cleanMobile = mobileNumber?.trim().orEmpty()

        if (cleanName.length < 2) {
            _uiState.value = SetupUiState.Error("Full name must be at least 2 characters")
            return
        }
        if (!cleanMobile.matches(Regex("^\\d{10}$"))) {
            _uiState.value = SetupUiState.Error("Mobile number must be exactly 10 digits")
            return
        }
        if (languageClassId.isNullOrBlank()) {
            _uiState.value = SetupUiState.Error("Please select a language class")
            return
        }
        if (technologyClassId.isNullOrBlank()) {
            _uiState.value = SetupUiState.Error("Please select a technology class")
            return
        }
        if (year.isNullOrBlank()) {
            _uiState.value = SetupUiState.Error("Please select your academic year")
            return
        }

        viewModelScope.launch {
            _uiState.value = SetupUiState.Loading
            profileRepository.setupProfile(
                name = name.trim(),
                languageClassId = languageClassId,
                technologyClassId = technologyClassId,
                enrollmentNumber = enrollmentNumber?.trim()?.ifBlank { null },
                year = year?.trim()?.ifBlank { null },
                mobileNumber = mobileNumber?.trim()?.ifBlank { null }
            )
                .onSuccess { _uiState.value = SetupUiState.Success(it) }
                .onFailure { error ->
                    if (error.message?.contains("already exists", ignoreCase = true) == true) {
                        profileRepository.getProfile()
                            .onSuccess { _uiState.value = SetupUiState.Success(it) }
                            .onFailure { _uiState.value = SetupUiState.Error(error.message ?: "Profile already exists") }
                    } else {
                        _uiState.value = SetupUiState.Error(error.message ?: "Setup failed")
                    }
                }
        }
    }

    // Backwards-compatibility
    fun setupProfile(name: String, enrollmentNumber: String, year: String) {
        completeProfile(name, null, null, enrollmentNumber, year)
    }
}
