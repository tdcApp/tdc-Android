package com.bagadbille.tdc.ui.mentor.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Announcement
import com.bagadbille.tdc.data.model.Schedule
import com.bagadbille.tdc.data.repository.AnnouncementRepository
import com.bagadbille.tdc.data.repository.AssignmentRepository
import com.bagadbille.tdc.data.repository.ClassRepository
import com.bagadbille.tdc.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class OverviewUiData(
    val todaySchedules: List<Schedule> = emptyList(),
    val weekSchedules: List<Schedule> = emptyList(),
    val announcements: List<Announcement> = emptyList(),
    val pendingSubmissions: Int = 0,
    val classNames: Map<String, String> = emptyMap(),
    val isPostingAnnouncement: Boolean = false
)

sealed class OverviewUiState {
    data object Loading : OverviewUiState()
    data class Success(val data: OverviewUiData) : OverviewUiState()
    data class Error(val message: String) : OverviewUiState()
}

sealed class OverviewEvent {
    data class ShowMessage(val message: String) : OverviewEvent()
}

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val scheduleRepo: ScheduleRepository,
    private val announcementRepo: AnnouncementRepository,
    private val assignmentRepo: AssignmentRepository,
    private val classRepo: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OverviewUiState>(OverviewUiState.Loading)
    val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OverviewEvent>()
    val events: SharedFlow<OverviewEvent> = _events.asSharedFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            if (_uiState.value !is OverviewUiState.Success) {
                _uiState.value = OverviewUiState.Loading
            }
            try {
                val schedules = scheduleRepo.getSchedules().getOrDefault(emptyList())
                val announcements = announcementRepo.getAnnouncements().getOrDefault(emptyList())
                val classNames = classRepo.getClasses().getOrDefault(emptyList()).associate { it.id to it.name }
                val allSubmissions = assignmentRepo.getAllSubmissions().getOrDefault(emptyList())
                val mentorAssignments = assignmentRepo.getMentorAssignments().getOrDefault(emptyList())
                val submittedIds = allSubmissions.map { it.assignmentId }.toSet()
                val pendingCount = mentorAssignments.count { it.id !in submittedIds }

                val todayDow = LocalDate.now().dayOfWeek.value // 1=Mon … 7=Sun
                val todaySchedules = schedules.filter { it.dayOfWeek == todayDow }
                    .sortedBy { it.startTime }

                _uiState.value = OverviewUiState.Success(
                    OverviewUiData(
                        todaySchedules = todaySchedules,
                        weekSchedules = schedules.sortedWith(compareBy({ it.dayOfWeek }, { it.startTime })),
                        announcements = announcements.take(5),
                        pendingSubmissions = pendingCount,
                        classNames = classNames
                    )
                )
            } catch (e: Exception) {
                _uiState.value = OverviewUiState.Error(e.message ?: "Failed to load overview")
            }
        }
    }

    fun postAnnouncement(title: String, content: String, audience: String, classId: String?) {
        val current = _uiState.value as? OverviewUiState.Success ?: return
        viewModelScope.launch {
            _uiState.value = OverviewUiState.Success(current.data.copy(isPostingAnnouncement = true))
            announcementRepo.createAnnouncement(title, content, audience, classId)
                .onSuccess {
                    _events.emit(OverviewEvent.ShowMessage("Announcement posted!"))
                    load()
                }
                .onFailure {
                    _uiState.value = OverviewUiState.Success(current.data.copy(isPostingAnnouncement = false))
                    _events.emit(OverviewEvent.ShowMessage(it.message ?: "Failed to post announcement"))
                }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            announcementRepo.deleteAnnouncement(id)
                .onSuccess {
                    _events.emit(OverviewEvent.ShowMessage("Announcement deleted"))
                    load()
                }
                .onFailure {
                    _events.emit(OverviewEvent.ShowMessage(it.message ?: "Failed to delete"))
                }
        }
    }

    fun createSchedule(
        classId: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        subject: String,
        room: String?
    ) {
        viewModelScope.launch {
            scheduleRepo.createSchedule(classId, dayOfWeek, startTime, endTime, subject, room)
                .onSuccess {
                    _events.emit(OverviewEvent.ShowMessage("Class scheduled successfully"))
                    load()
                }
                .onFailure {
                    _events.emit(OverviewEvent.ShowMessage(it.message ?: "Failed to create schedule"))
                }
        }
    }

    fun deleteSchedule(id: String) {
        viewModelScope.launch {
            scheduleRepo.deleteSchedule(id)
                .onSuccess {
                    _events.emit(OverviewEvent.ShowMessage("Schedule removed"))
                    load()
                }
                .onFailure {
                    _events.emit(OverviewEvent.ShowMessage(it.message ?: "Failed to delete schedule"))
                }
        }
    }
}
