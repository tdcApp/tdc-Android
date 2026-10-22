package com.bagadbille.tdc.ui.mentor.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.model.UserProfile
import com.bagadbille.tdc.data.repository.ClassRepository
import com.bagadbille.tdc.data.repository.RosterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BatchItem(
    val classInfo: ClassInfo,
    val students: List<UserProfile> = emptyList(),
    val isLoadingStudents: Boolean = false
)

sealed class RosterUiState {
    data object Loading : RosterUiState()
    data class Success(val batches: List<BatchItem>) : RosterUiState()
    data class Error(val message: String) : RosterUiState()
}

@HiltViewModel
class RosterViewModel @Inject constructor(
    private val classRepo: ClassRepository,
    private val rosterRepo: RosterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RosterUiState>(RosterUiState.Loading)
    val uiState: StateFlow<RosterUiState> = _uiState.asStateFlow()

    private val studentCache = mutableMapOf<String, List<UserProfile>>()

    fun load() {
        viewModelScope.launch {
            _uiState.value = RosterUiState.Loading
            try {
                val classes = classRepo.getClasses().getOrThrow()
                val batches = classes.map { cls ->
                    BatchItem(
                        classInfo = cls,
                        students = studentCache[cls.id] ?: emptyList()
                    )
                }.sortedBy { it.classInfo.name }

                _uiState.value = RosterUiState.Success(batches)
            } catch (e: Exception) {
                _uiState.value = RosterUiState.Error(e.message ?: "Failed to load classes")
            }
        }
    }

    fun loadStudentsForBatch(classId: String) {
        if (studentCache.containsKey(classId)) return
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is RosterUiState.Success) {
                _uiState.value = RosterUiState.Success(
                    currentState.batches.map {
                        if (it.classInfo.id == classId) it.copy(isLoadingStudents = true) else it
                    }
                )
            }

            val students = rosterRepo.getStudentsByClass(classId).getOrDefault(emptyList())
            studentCache[classId] = students

            val updatedState = _uiState.value
            if (updatedState is RosterUiState.Success) {
                _uiState.value = RosterUiState.Success(
                    updatedState.batches.map {
                        if (it.classInfo.id == classId) {
                            it.copy(
                                students = students,
                                isLoadingStudents = false,
                                classInfo = it.classInfo.copy(studentCount = students.size)
                            )
                        } else it
                    }
                )
            }
        }
    }
}

