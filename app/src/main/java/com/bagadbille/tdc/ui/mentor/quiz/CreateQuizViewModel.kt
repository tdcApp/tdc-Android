package com.bagadbille.tdc.ui.mentor.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.QuizQuestionDraft
import com.bagadbille.tdc.data.repository.ClassRepository
import com.bagadbille.tdc.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateQuizUiState(
    val title: String = "",
    val audience: String = "all",
    val classId: String? = null,
    val classes: Map<String, String> = emptyMap(),
    val questions: List<QuizQuestionDraft> = listOf(
        QuizQuestionDraft(question = "", optionA = "", optionB = "", optionC = "", optionD = "", correctOption = "a")
    ),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed class CreateQuizEvent {
    data object QuizSaved : CreateQuizEvent()
    data class ShowMessage(val message: String) : CreateQuizEvent()
}

@HiltViewModel
class CreateQuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateQuizUiState())
    val uiState: StateFlow<CreateQuizUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CreateQuizEvent>()
    val events: SharedFlow<CreateQuizEvent> = _events.asSharedFlow()

    init {
        loadClasses()
    }

    private fun loadClasses() {
        viewModelScope.launch {
            val classList = classRepository.getClasses().getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                classes = classList.associate { it.id to it.name }
            )
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title, errorMessage = null)
    }

    fun updateAudience(audience: String) {
        _uiState.value = _uiState.value.copy(
            audience = audience,
            classId = if (audience == "all") null else _uiState.value.classId,
            errorMessage = null
        )
    }

    fun updateClassId(classId: String?) {
        _uiState.value = _uiState.value.copy(classId = classId, errorMessage = null)
    }

    fun addQuestion() {
        val current = _uiState.value.questions.toMutableList()
        current.add(QuizQuestionDraft(question = "", optionA = "", optionB = "", optionC = "", optionD = "", correctOption = "a"))
        _uiState.value = _uiState.value.copy(questions = current, errorMessage = null)
    }

    fun removeQuestion(index: Int) {
        val current = _uiState.value.questions.toMutableList()
        if (current.size > 1 && index in current.indices) {
            current.removeAt(index)
            _uiState.value = _uiState.value.copy(questions = current, errorMessage = null)
        }
    }

    fun updateQuestion(index: Int, draft: QuizQuestionDraft) {
        val current = _uiState.value.questions.toMutableList()
        if (index in current.indices) {
            current[index] = draft
            _uiState.value = _uiState.value.copy(questions = current, errorMessage = null)
        }
    }

    fun saveQuiz() {
        val state = _uiState.value
        val title = state.title.trim()
        if (title.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter a quiz title")
            return
        }

        if (state.questions.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Please add at least 1 question")
            return
        }

        for (i in state.questions.indices) {
            val q = state.questions[i]
            if (q.question.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Please fill in question prompt for Question ${i + 1}")
                return
            }
            if (q.optionA.isBlank() || q.optionB.isBlank() || q.optionC.isBlank() || q.optionD.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Please fill all 4 options for Question ${i + 1}")
                return
            }
            if (q.correctOption.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Please select the correct option for Question ${i + 1}")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            quizRepository.createQuiz(
                title = title,
                audience = state.audience,
                classId = state.classId,
                questions = state.questions
            ).onSuccess {
                _events.emit(CreateQuizEvent.ShowMessage("Quiz created successfully!"))
                _events.emit(CreateQuizEvent.QuizSaved)
            }.onFailure {
                _uiState.value = state.copy(isSaving = false, errorMessage = it.message ?: "Failed to save quiz")
            }
        }
    }
}
