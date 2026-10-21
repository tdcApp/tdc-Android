package com.bagadbille.tdc.ui.home.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.model.Schedule
import com.bagadbille.tdc.data.repository.ClassRepository
import com.bagadbille.tdc.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassesData(
    val enrolledClasses: List<ClassInfo> = emptyList(),
    val schedules: List<Schedule> = emptyList()
)

sealed class ClassesUiState {
    data object Loading : ClassesUiState()
    data class Success(val data: ClassesData) : ClassesUiState()
    data class Error(val message: String) : ClassesUiState()
}

@HiltViewModel
class ClassesViewModel @Inject constructor(
    private val repo: ClassRepository,
    private val scheduleRepo: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClassesUiState>(ClassesUiState.Loading)
    val uiState: StateFlow<ClassesUiState> = _uiState.asStateFlow()

    init {
        loadClasses()
    }

    fun loadClasses() {
        viewModelScope.launch {
            _uiState.value = ClassesUiState.Loading
            try {
                coroutineScope {
                    val myDef = async { repo.getMyClasses().getOrDefault(emptyList()) }
                    val schedDef = async { scheduleRepo.getSchedules().getOrDefault(emptyList()) }

                    val myClasses = myDef.await()
                    val schedules = schedDef.await()

                    _uiState.value = ClassesUiState.Success(
                        ClassesData(
                            enrolledClasses = myClasses,
                            schedules = schedules
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ClassesUiState.Error(e.message ?: "Failed to load upcoming classes")
            }
        }
    }
}
