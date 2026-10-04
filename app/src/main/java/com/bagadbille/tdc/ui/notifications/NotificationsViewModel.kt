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
    val announcements: List<Announcement> = emptyList()
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

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = NotificationsUiState.Loading
            try {
                val notifs = notifRepo.getNotifications().getOrDefault(emptyList())
                val announcements = announcementRepo.getAnnouncements().getOrDefault(emptyList())
                _uiState.value = NotificationsUiState.Success(NotificationsData(notifs, announcements))
            } catch (e: Exception) {
                _uiState.value = NotificationsUiState.Error(e.message ?: "Failed to load")
            }
        }
    }

    fun markAsRead(id: String) { viewModelScope.launch { notifRepo.markAsRead(id); loadAll() } }
}
