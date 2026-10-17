package com.bagadbille.tdc.ui.mentor.assignments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.model.AssignmentSubmission
import com.bagadbille.tdc.data.repository.AssignmentRepository
import com.bagadbille.tdc.data.repository.RosterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubmissionItem(
    val submission: AssignmentSubmission,
    val studentName: String
)

sealed class SubmissionsUiState {
    data object Loading : SubmissionsUiState()
    data class Success(
        val assignment: Assignment,
        val submissions: List<SubmissionItem>,
        val totalStudents: Int
    ) : SubmissionsUiState()
    data class Error(val message: String) : SubmissionsUiState()
}

@HiltViewModel
class AssignmentSubmissionsViewModel @Inject constructor(
    private val assignmentRepo: AssignmentRepository,
    private val rosterRepo: RosterRepository,
    private val dataStoreManager: DataStoreManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val assignmentId: String = checkNotNull(savedStateHandle["assignmentId"])

    private val _uiState = MutableStateFlow<SubmissionsUiState>(SubmissionsUiState.Loading)
    val uiState: StateFlow<SubmissionsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = SubmissionsUiState.Loading
            try {
                val currentUid = dataStoreManager.firebaseUid.firstOrNull()
                val currentName = dataStoreManager.userName.firstOrNull()

                val assignment = assignmentRepo.getAssignmentById(assignmentId).getOrThrow()
                val rawSubmissions = assignmentRepo.getSubmissionsForAssignment(assignmentId).getOrDefault(emptyList())
                val classId = assignment.classId
                val enrolledStudents = rosterRepo.getStudentsByClass(classId).getOrDefault(emptyList())
                val studentMap = enrolledStudents.associateBy { it.firebaseUid }

                val items = rawSubmissions.map { sub ->
                    val resolvedName = sub.studentName?.ifBlank { null }?.takeIf { it != "Student" }
                        ?: studentMap[sub.menteeUid]?.name?.ifBlank { null }
                        ?: (if (sub.menteeUid == currentUid && !currentName.isNullOrBlank()) currentName else null)
                        ?: studentMap[sub.menteeUid]?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
                        ?: sub.studentEmail?.substringBefore("@")?.takeIf { it.isNotBlank() }
                        ?: "Candidate (${sub.menteeUid.take(6)})"

                    SubmissionItem(
                        submission = sub,
                        studentName = resolvedName
                    )
                }.sortedByDescending { it.submission.submittedAt }

                val totalStudents = enrolledStudents.size

                _uiState.value = SubmissionsUiState.Success(
                    assignment = assignment,
                    submissions = items,
                    totalStudents = totalStudents
                )
            } catch (e: Exception) {
                _uiState.value = SubmissionsUiState.Error(e.message ?: "Failed to load submissions")
            }
        }
    }
}
