package com.bagadbille.tdc.ui.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.model.AssignmentSubmission
import com.bagadbille.tdc.data.repository.AssignmentRepository
import com.bagadbille.tdc.data.repository.ClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

data class AssignmentItem(
    val assignment: Assignment,
    val className: String? = null,
    val submission: AssignmentSubmission? = null,
    val isOverdue: Boolean = false,
    val statusText: String = ""
)

sealed class AssignmentsUiState {
    data object Loading : AssignmentsUiState()
    data class Success(
        val ongoingAssignments: List<AssignmentItem>,
        val pastAssignments: List<AssignmentItem>,
        val totalPendingCount: Int = 0,
        val totalSubmittedCount: Int = 0
    ) : AssignmentsUiState()
    data class Error(val message: String) : AssignmentsUiState()
}

@HiltViewModel
class AssignmentsViewModel @Inject constructor(
    private val repo: AssignmentRepository,
    private val classRepo: ClassRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AssignmentsUiState>(AssignmentsUiState.Loading)
    val uiState: StateFlow<AssignmentsUiState> = _uiState.asStateFlow()

    init { loadAssignments() }

    fun loadAssignments() {
        viewModelScope.launch {
            _uiState.value = AssignmentsUiState.Loading
            try {
                val assignments = repo.getAssignments().getOrDefault(emptyList())
                val submissions = repo.getSubmissions().getOrDefault(emptyMap())
                val classes = classRepo.getClasses().getOrDefault(emptyList()).associateBy { it.id }

                val now = Instant.now()

                val items = assignments.map { assignment ->
                    val submission = submissions[assignment.id]
                    val dueInstant = assignment.dueAt?.let {
                        try { Instant.parse(it) } catch (_: Exception) { null }
                    }

                    val isPastDue = dueInstant != null && dueInstant.isBefore(now)
                    val isSubmitted = submission != null
                    val isOverdue = isPastDue && !isSubmitted

                    val statusText = when {
                        isSubmitted -> "Submitted"
                        dueInstant != null -> {
                            val duration = Duration.between(now, dueInstant)
                            val days = duration.toDays()
                            val hours = duration.toHours()
                            when {
                                days < 0 -> "Overdue"
                                days == 0L && hours > 0 -> "Due in ${hours}h"
                                days == 0L -> "Due soon"
                                days == 1L -> "Due tomorrow"
                                else -> "Due in $days days"
                            }
                        }
                        else -> "No due date"
                    }

                    AssignmentItem(
                        assignment = assignment,
                        className = classes[assignment.classId]?.name,
                        submission = submission,
                        isOverdue = isOverdue,
                        statusText = statusText
                    )
                }

                val ongoing = items.filter { item ->
                    val dueInstant = item.assignment.dueAt?.let { try { Instant.parse(it) } catch (_: Exception) { null } }
                    dueInstant == null || dueInstant.isAfter(now)
                }

                val past = items.filter { item ->
                    val dueInstant = item.assignment.dueAt?.let { try { Instant.parse(it) } catch (_: Exception) { null } }
                    dueInstant != null && dueInstant.isBefore(now)
                }

                val pendingCount = items.count { it.submission == null }
                val submittedCount = items.count { it.submission != null }

                _uiState.value = AssignmentsUiState.Success(
                    ongoingAssignments = ongoing,
                    pastAssignments = past,
                    totalPendingCount = pendingCount,
                    totalSubmittedCount = submittedCount
                )
            } catch (e: Exception) {
                _uiState.value = AssignmentsUiState.Error(e.message ?: "Failed to load assignments")
            }
        }
    }
}
