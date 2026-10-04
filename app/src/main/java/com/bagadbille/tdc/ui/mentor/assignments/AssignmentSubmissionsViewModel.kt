package com.bagadbille.tdc.ui.mentor.assignments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.mock.MockUsers
import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.model.AssignmentSubmission
import com.bagadbille.tdc.data.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                val assignment = assignmentRepo.getAssignmentById(assignmentId).getOrThrow()
                val rawSubmissions = assignmentRepo.getSubmissionsForAssignment(assignmentId).getOrDefault(emptyList())
                val items = rawSubmissions.map { sub ->
                    SubmissionItem(
                        submission = sub,
                        studentName = MockUsers.findById(sub.menteeUid)?.name ?: sub.menteeUid
                    )
                }.sortedByDescending { it.submission.submittedAt }

                // Total students enrolled in this assignment's class
                val classId = assignment.classId
                val totalStudents = MockUsers.studentsInClass(classId).size

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
