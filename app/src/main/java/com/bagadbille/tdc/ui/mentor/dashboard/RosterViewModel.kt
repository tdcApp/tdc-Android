package com.bagadbille.tdc.ui.mentor.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.model.UserProfile
import com.bagadbille.tdc.data.remote.dto.TeacherUserDto
import com.bagadbille.tdc.data.repository.ClassRepository
import com.bagadbille.tdc.data.repository.RosterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val rosterRepo: RosterRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<RosterUiState>(RosterUiState.Loading)
    val uiState: StateFlow<RosterUiState> = _uiState.asStateFlow()

    val isAdmin: StateFlow<Boolean> = dataStoreManager.userRole
        .map { it?.equals("admin", ignoreCase = true) == true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _teachers = MutableStateFlow<List<TeacherUserDto>>(emptyList())
    val teachers: StateFlow<List<TeacherUserDto>> = _teachers.asStateFlow()

    private val studentCache = mutableMapOf<String, List<UserProfile>>()

    fun load() {
        viewModelScope.launch {
            _uiState.value = RosterUiState.Loading
            try {
                _teachers.value = classRepo.getTeachers().getOrDefault(emptyList())

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

    fun createClass(
        name: String,
        type: String,
        mentorUid: String,
        onFinished: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = classRepo.createClass(name, type, mentorUid)
            result.onSuccess {
                load()
                onFinished(true, null)
            }.onFailure {
                onFinished(false, it.message ?: "Failed to create class")
            }
        }
    }

    fun updateClass(
        id: String,
        name: String,
        type: String,
        mentorUid: String,
        onFinished: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = classRepo.updateClass(id, name, type, mentorUid)
            result.onSuccess {
                studentCache.remove(id)
                load()
                onFinished(true, null)
            }.onFailure {
                onFinished(false, it.message ?: "Failed to update class")
            }
        }
    }
}
