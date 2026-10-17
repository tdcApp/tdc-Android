package com.bagadbille.tdc.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Announcement
import com.bagadbille.tdc.data.model.AppNotification
import com.bagadbille.tdc.data.repository.AnnouncementRepository
import com.bagadbille.tdc.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsData(
    val notifications: List<AppNotification> = emptyList(),
    val announcements: List<Announcement> = emptyList(),
    val isRefreshing: Boolean = false
)

sealed class NotificationsUiState {
    data object Loading : NotificationsUiState()
    data class Success(val data: NotificationsData) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notifRepo: NotificationRepository,
    private val announcementRepo: AnnouncementRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init { loadAll() }

    fun loadAll(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentData = (_uiState.value as? NotificationsUiState.Success)?.data
            if (isPullToRefresh && currentData != null) {
                _uiState.value = NotificationsUiState.Success(currentData.copy(isRefreshing = true))
            } else if (!isPullToRefresh) {
                _uiState.value = NotificationsUiState.Loading
            }
            try {
                val notifs = notifRepo.getNotifications().getOrDefault(emptyList())
                val announcements = announcementRepo.getAnnouncements().getOrDefault(emptyList())
                _uiState.value = NotificationsUiState.Success(
                    NotificationsData(
                        notifications = notifs,
                        announcements = announcements,
                        isRefreshing = false
                    )
                )
            } catch (e: Exception) {
                if (currentData != null) {
                    _uiState.value = NotificationsUiState.Success(currentData.copy(isRefreshing = false))
                } else {
                    _uiState.value = NotificationsUiState.Error(e.message ?: "Failed to load")
                }
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notifRepo.markAsRead(id)
            // Silently refresh list state to reflect the updated isRead
            val currentData = (_uiState.value as? NotificationsUiState.Success)?.data
            if (currentData != null) {
                val updatedNotifs = currentData.notifications.map {
                    if (it.id == id) it.copy(isRead = true) else it
                }
                _uiState.value = NotificationsUiState.Success(currentData.copy(notifications = updatedNotifs))
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notifRepo.markAllAsRead()
            val currentData = (_uiState.value as? NotificationsUiState.Success)?.data
            if (currentData != null) {
                val updatedNotifs = currentData.notifications.map { it.copy(isRead = true) }
                _uiState.value = NotificationsUiState.Success(currentData.copy(notifications = updatedNotifs))
            }
        }
    }
}
