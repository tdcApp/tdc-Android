package com.bagadbille.tdc.ui.home.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Announcement
import com.bagadbille.tdc.data.repository.AnnouncementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GeneralUiState {
    data object Loading : GeneralUiState()
    data class Success(val announcements: List<Announcement>) : GeneralUiState()
    data class Error(val message: String) : GeneralUiState()
}

@HiltViewModel
class GeneralViewModel @Inject constructor(private val repo: AnnouncementRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<GeneralUiState>(GeneralUiState.Loading)
    val uiState: StateFlow<GeneralUiState> = _uiState.asStateFlow()
    init { loadAnnouncements() }
    fun loadAnnouncements() {
        viewModelScope.launch {
            _uiState.value = GeneralUiState.Loading
            repo.getAnnouncements()
                .onSuccess { _uiState.value = GeneralUiState.Success(it) }
                .onFailure { _uiState.value = GeneralUiState.Error(it.message ?: "Failed to load") }
        }
    }
}
