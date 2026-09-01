package com.bagadbille.tdc.ui.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.model.AssignmentStatus
import com.bagadbille.tdc.data.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AssignmentsUiState {
    data object Loading : AssignmentsUiState()
    data class Success(
        val ongoingAssignments: List<Assignment>,
        val pastAssignments: List<Assignment>
    ) : AssignmentsUiState()
    data class Error(val message: String) : AssignmentsUiState()
}

@HiltViewModel
class AssignmentsViewModel @Inject constructor(
    private val repo: AssignmentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AssignmentsUiState>(AssignmentsUiState.Loading)
    val uiState: StateFlow<AssignmentsUiState> = _uiState.asStateFlow()

    init { loadAssignments() }

    fun loadAssignments() {
        viewModelScope.launch {
            _uiState.value = AssignmentsUiState.Loading
            repo.getAssignments()
                .onSuccess { assignments ->
                    _uiState.value = AssignmentsUiState.Success(
                        ongoingAssignments = assignments.filter { it.status == AssignmentStatus.ONGOING },
                        pastAssignments = assignments.filter { it.status == AssignmentStatus.PAST }
                    )
                }
                .onFailure { _uiState.value = AssignmentsUiState.Error(it.message ?: "Failed to load assignments") }
        }
    }
}
