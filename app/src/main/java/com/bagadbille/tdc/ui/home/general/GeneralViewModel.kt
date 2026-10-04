package com.bagadbille.tdc.ui.home.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Quiz
import com.bagadbille.tdc.data.model.Schedule
import com.bagadbille.tdc.data.repository.QuizRepository
import com.bagadbille.tdc.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardData(
    val schedules: List<Schedule> = emptyList(),
    val upcomingQuizzes: List<Quiz> = emptyList()
)

sealed class GeneralUiState {
    data object Loading : GeneralUiState()
    data class Success(val data: DashboardData) : GeneralUiState()
    data class Error(val message: String) : GeneralUiState()
}

@HiltViewModel
class GeneralViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val quizRepository: QuizRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<GeneralUiState>(GeneralUiState.Loading)
    val uiState: StateFlow<GeneralUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = GeneralUiState.Loading
            try {
                val schedules = scheduleRepository.getSchedules().getOrDefault(emptyList())
                val quizzes = quizRepository.getQuizzes().getOrDefault(emptyList())

                _uiState.value = GeneralUiState.Success(
                    DashboardData(
                        schedules = schedules,
                        upcomingQuizzes = quizzes.take(3)
                    )
                )
            } catch (e: Exception) {
                _uiState.value = GeneralUiState.Error(e.message ?: "Failed to load dashboard")
            }
        }
    }
}
