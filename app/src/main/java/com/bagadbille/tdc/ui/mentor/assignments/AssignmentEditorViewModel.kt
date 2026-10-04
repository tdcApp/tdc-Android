package com.bagadbille.tdc.ui.mentor.assignments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Assignment
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.repository.AssignmentRepository
import com.bagadbille.tdc.data.repository.ClassRepository
import com.bagadbille.tdc.ui.util.TdcDates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

data class AssignmentEditorState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val classes: List<ClassInfo> = emptyList(),
    val title: String = "",
    val description: String = "",
    val classId: String? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime = LocalTime.of(23, 59),
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class AssignmentEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val assignmentRepo: AssignmentRepository,
    private val classRepo: ClassRepository
) : ViewModel() {

    private val assignmentId: String? = savedStateHandle["assignmentId"]
    private var original: Assignment? = null

    private val _state = MutableStateFlow(AssignmentEditorState(isEditing = assignmentId != null))
    val state: StateFlow<AssignmentEditorState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val classes = classRepo.getClasses().getOrDefault(emptyList())
            if (assignmentId == null) {
                _state.update { it.copy(isLoading = false, classes = classes) }
                return@launch
            }
            assignmentRepo.getAssignmentById(assignmentId)
                .onSuccess { a ->
                    original = a
                    val due = TdcDates.parse(a.dueAt)?.atZone(ZoneId.systemDefault())
                    _state.update {
                        it.copy(
                            isLoading = false,
                            classes = classes,
                            title = a.title,
                            description = a.description.orEmpty(),
                            classId = a.classId,
                            dueDate = due?.toLocalDate(),
                            dueTime = due?.toLocalTime()?.withSecond(0)?.withNano(0) ?: LocalTime.of(23, 59)
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, classes = classes, error = e.message ?: "Assignment not found") }
                }
        }
    }

    fun onTitleChange(value: String) = _state.update { it.copy(title = value, error = null) }
    fun onDescriptionChange(value: String) = _state.update { it.copy(description = value, error = null) }
    fun onClassSelected(id: String) = _state.update { it.copy(classId = id, error = null) }
    fun onDateSelected(date: LocalDate) = _state.update { it.copy(dueDate = date, error = null) }
    fun onTimeSelected(time: LocalTime) = _state.update { it.copy(dueTime = time, error = null) }

    fun save() {
        val s = _state.value
        val error = when {
            s.title.isBlank() -> "Title is required"
            s.classId == null -> "Pick a batch"
            s.dueDate == null -> "Pick a due date"
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(error = error) }
            return
        }

        val dueInstant = ZonedDateTime.of(s.dueDate, s.dueTime, ZoneId.systemDefault()).toInstant()
        // Editing can keep a past date (e.g. fixing a typo on an old assignment); new ones must be in the future
        if (!s.isEditing && dueInstant.isBefore(Instant.now())) {
            _state.update { it.copy(error = "Due date must be in the future") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val title = s.title.trim()
            val description = s.description.trim().ifBlank { null }
            val classId = s.classId!!
            val result = original?.let {
                assignmentRepo.updateAssignment(
                    it.copy(title = title, description = description, classId = classId, dueAt = dueInstant.toString())
                )
            } ?: assignmentRepo.createAssignment(title, description, classId, dueInstant.toString())

            result
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { e -> _state.update { it.copy(isSaving = false, error = e.message ?: "Failed to save") } }
        }
    }
}
