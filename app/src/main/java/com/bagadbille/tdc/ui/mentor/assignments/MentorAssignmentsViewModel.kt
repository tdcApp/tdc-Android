package com.bagadbille.tdc.ui.mentor.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.repository.AssignmentRepository
import com.bagadbille.tdc.data.repository.ClassRepository
import com.bagadbille.tdc.data.repository.RosterRepository
import com.bagadbille.tdc.ui.util.TdcDates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class MentorAssignmentItem(
    val assignment: Assignment,
    val className: String?,
    val submittedCount: Int,
    val totalStudents: Int
)

sealed class MentorAssignmentsUiState {
    data object Loading : MentorAssignmentsUiState()
    data class Success(
        val ongoing: List<MentorAssignmentItem>,
        val past: List<MentorAssignmentItem>
    ) : MentorAssignmentsUiState()
    data class Error(val message: String) : MentorAssignmentsUiState()
}

@HiltViewModel
class MentorAssignmentsViewModel @Inject constructor(
    private val assignmentRepo: AssignmentRepository,
    private val classRepo: ClassRepository,
    private val rosterRepo: RosterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MentorAssignmentsUiState>(MentorAssignmentsUiState.Loading)
    val uiState: StateFlow<MentorAssignmentsUiState> = _uiState.asStateFlow()

    /** Called whenever the screen is shown, so creates/edits/deletes show up on return. */
    fun load() {
        viewModelScope.launch {
            // Refresh silently if we already have data
            if (_uiState.value !is MentorAssignmentsUiState.Success) {
                _uiState.value = MentorAssignmentsUiState.Loading
            }
            try {
                val assignments = assignmentRepo.getMentorAssignments().getOrThrow()
                val submissions = assignmentRepo.getAllSubmissions().getOrDefault(emptyList())
                val classNames = classRepo.getClasses().getOrDefault(emptyList()).associate { it.id to it.name }
                val studentCounts = rosterRepo.getClassStudentCounts(assignments.map { it.classId })
                    .getOrDefault(emptyMap())

                val submittedByAssignment = submissions.groupingBy { it.assignmentId }.eachCount()
                val now = Instant.now()

                val items = assignments.map {
                    MentorAssignmentItem(
                        assignment = it,
                        className = classNames[it.classId],
                        submittedCount = submittedByAssignment[it.id] ?: 0,
                        totalStudents = studentCounts[it.classId] ?: 0
                    )
                }

                val (past, ongoing) = items.partition { item ->
                    TdcDates.parse(item.assignment.dueAt)?.isBefore(now) == true
                }

                _uiState.value = MentorAssignmentsUiState.Success(
                    ongoing = ongoing.sortedBy { TdcDates.parse(it.assignment.dueAt) ?: Instant.MAX },
                    past = past.sortedByDescending { TdcDates.parse(it.assignment.dueAt) }
                )
            } catch (e: Exception) {
                _uiState.value = MentorAssignmentsUiState.Error(e.message ?: "Failed to load assignments")
            }
        }
    }
}
