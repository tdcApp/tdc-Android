package com.bagadbille.tdc.ui.assignments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AssignmentDetailUiState {
    data object Loading : AssignmentDetailUiState()
    data class Success(val assignment: Assignment) : AssignmentDetailUiState()
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

    private fun loadAssignment() {
        viewModelScope.launch {
            _uiState.value = AssignmentDetailUiState.Loading
            repo.getAssignmentById(assignmentId)
                .onSuccess { _uiState.value = AssignmentDetailUiState.Success(it) }
                .onFailure { _uiState.value = AssignmentDetailUiState.Error(it.message ?: "Failed to load assignment") }
        }
    }
}
