package com.bagadbille.tdc.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.AppNotification
import com.bagadbille.tdc.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotificationsUiState {
    data object Loading : NotificationsUiState()
    data class Success(val notifications: List<AppNotification>) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(private val repo: NotificationRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()
    init { loadNotifications() }
    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationsUiState.Loading
            repo.getNotifications()
                .onSuccess { _uiState.value = NotificationsUiState.Success(it) }
                .onFailure { _uiState.value = NotificationsUiState.Error(it.message ?: "Failed to load") }
        }
    }
    fun markAsRead(id: String) { viewModelScope.launch { repo.markAsRead(id); loadNotifications() } }
}
