package com.bagadbille.tdc.ui.assignments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.model.AssignmentSubmission
import com.bagadbille.tdc.data.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AssignmentDetailUiState {
    data object Loading : AssignmentDetailUiState()
    data class Success(
        val assignment: Assignment,
        val submission: AssignmentSubmission? = null,
        val isSubmitting: Boolean = false,
        val submitError: String? = null
    ) : AssignmentDetailUiState()
    data class Error(val message: String) : AssignmentDetailUiState()
}

@HiltViewModel
class AssignmentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: AssignmentRepository
) : ViewModel() {
    private val assignmentId: String = savedStateHandle["assignmentId"] ?: ""

    private val _uiState = MutableStateFlow<AssignmentDetailUiState>(AssignmentDetailUiState.Loading)
    val uiState: StateFlow<AssignmentDetailUiState> = _uiState.asStateFlow()

    init { loadAssignment() }

    fun loadAssignment() {
        viewModelScope.launch {
            _uiState.value = AssignmentDetailUiState.Loading
            repo.getAssignmentById(assignmentId)
                .onSuccess { assignment ->
                    val submission = repo.getSubmission(assignmentId).getOrNull()
                    _uiState.value = AssignmentDetailUiState.Success(assignment, submission)
                }
                .onFailure { _uiState.value = AssignmentDetailUiState.Error(it.message ?: "Failed to load assignment") }
        }
    }

    fun submit(submissionUrl: String, onDone: () -> Unit = {}) {
        val currentState = _uiState.value as? AssignmentDetailUiState.Success ?: return
        if (submissionUrl.isBlank()) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSubmitting = true, submitError = null)
            repo.submitAssignment(assignmentId, submissionUrl)
                .onSuccess { sub ->
                    _uiState.value = currentState.copy(submission = sub, isSubmitting = false)
                    onDone()
                }
                .onFailure {
                    _uiState.value = currentState.copy(isSubmitting = false, submitError = it.message ?: "Submission failed")
                }
        }
    }
}
