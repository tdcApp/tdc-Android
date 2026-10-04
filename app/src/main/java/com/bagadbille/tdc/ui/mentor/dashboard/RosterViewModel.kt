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
    val students: List<UserProfile>
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

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = RosterUiState.Loading
            try {
                val classes = classRepo.getClasses().getOrThrow()
                val batches = classes.map { cls ->
                    BatchItem(
                        classInfo = cls,
                        students = rosterRepo.getStudentsByClass(cls.id).getOrDefault(emptyList())
                    )
                }.filter { it.students.isNotEmpty() }

                _uiState.value = RosterUiState.Success(batches)
            } catch (e: Exception) {
                _uiState.value = RosterUiState.Error(e.message ?: "Failed to load roster")
            }
        }
    }
}
