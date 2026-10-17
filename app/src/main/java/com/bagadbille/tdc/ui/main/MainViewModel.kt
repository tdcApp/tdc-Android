package com.bagadbille.tdc.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.model.isMentorRole
import com.bagadbille.tdc.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    dataStoreManager: DataStoreManager,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    /** null until the stored role is read, so the student UI never flashes for mentors. */
    val isMentor: StateFlow<Boolean?> = dataStoreManager.userRole
        .map { isMentorRole(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val hasUnreadNotifications: StateFlow<Boolean> = notificationRepository.hasUnread()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        refreshNotifications()
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            notificationRepository.getNotifications()
        }
    }
}

