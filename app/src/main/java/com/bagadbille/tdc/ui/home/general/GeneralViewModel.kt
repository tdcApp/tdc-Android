package com.bagadbille.tdc.ui.home.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.model.Announcement
import com.bagadbille.tdc.data.model.Quiz
import com.bagadbille.tdc.data.model.Schedule
import com.bagadbille.tdc.data.repository.AnnouncementRepository
import com.bagadbille.tdc.data.repository.ProfileRepository
import com.bagadbille.tdc.data.repository.QuizRepository
import com.bagadbille.tdc.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardData(
    val userName: String = "",
    val schedules: List<Schedule> = emptyList(),
    val upcomingQuizzes: List<Quiz> = emptyList(),
    val recentAnnouncements: List<Announcement> = emptyList()
)

sealed class GeneralUiState {
    data object Loading : GeneralUiState()
    data class Success(val data: DashboardData) : GeneralUiState()
    data class Error(val message: String) : GeneralUiState()
}

@HiltViewModel
class GeneralViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val quizRepository: QuizRepository,
    private val profileRepository: ProfileRepository,
    private val announcementRepository: AnnouncementRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<GeneralUiState>(GeneralUiState.Loading)
    val uiState: StateFlow<GeneralUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = GeneralUiState.Loading
            try {
                coroutineScope {
                    val cachedName = dataStoreManager.userName.firstOrNull()?.trim()?.split(" ")?.firstOrNull()
                    val sDef = async { scheduleRepository.getSchedules().getOrDefault(emptyList()) }
                    val qDef = async { quizRepository.getQuizzes().getOrDefault(emptyList()) }
                    val pDef = if (cachedName.isNullOrBlank()) {
                        async { profileRepository.getProfile().getOrNull()?.name?.trim()?.split(" ")?.firstOrNull() }
                    } else null
                    val aDef = async { announcementRepository.getAnnouncements().getOrDefault(emptyList()) }

                    val schedules = sDef.await()
                    val quizzes = qDef.await()
                    val userName = cachedName ?: pDef?.await() ?: ""
                    val announcements = aDef.await()

                    _uiState.value = GeneralUiState.Success(
                        DashboardData(
                            userName = userName,
                            schedules = schedules,
                            upcomingQuizzes = quizzes.take(3),
                            recentAnnouncements = announcements.take(3)
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = GeneralUiState.Error(e.message ?: "Failed to load dashboard")
            }
        }
    }
}
