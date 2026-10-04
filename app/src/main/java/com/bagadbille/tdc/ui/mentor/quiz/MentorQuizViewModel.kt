package com.bagadbille.tdc.ui.mentor.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Quiz
import com.bagadbille.tdc.data.model.QuizStats
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

data class MentorQuizItem(
    val quiz: Quiz,
    val className: String?,
    val questionCount: Int,
    val stats: QuizStats?
)

sealed class MentorQuizUiState {
    data object Loading : MentorQuizUiState()
    data class Success(val quizzes: List<MentorQuizItem>) : MentorQuizUiState()
    data class Error(val message: String) : MentorQuizUiState()
}

sealed class MentorQuizEvent {
    data class ShowMessage(val message: String) : MentorQuizEvent()
}

@HiltViewModel
class MentorQuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val classRepository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MentorQuizUiState>(MentorQuizUiState.Loading)
    val uiState: StateFlow<MentorQuizUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<MentorQuizEvent>()
    val events: SharedFlow<MentorQuizEvent> = _events.asSharedFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            if (_uiState.value !is MentorQuizUiState.Success) {
                _uiState.value = MentorQuizUiState.Loading
            }
            try {
                val quizzes = quizRepository.getQuizzes().getOrDefault(emptyList())
                val classes = classRepository.getClasses().getOrDefault(emptyList()).associate { it.id to it.name }

                val items = quizzes.map { quiz ->
                    val questions = quizRepository.getQuizQuestions(quiz.id).getOrDefault(emptyList())
                    val stats = quizRepository.getQuizStats(quiz.id).getOrNull()
                    MentorQuizItem(
                        quiz = quiz,
                        className = quiz.classId?.let { classes[it] },
                        questionCount = questions.size,
                        stats = stats
                    )
                }
                _uiState.value = MentorQuizUiState.Success(items)
            } catch (e: Exception) {
                _uiState.value = MentorQuizUiState.Error(e.message ?: "Failed to load quizzes")
            }
        }
    }

    fun deleteQuiz(quizId: String) {
        viewModelScope.launch {
            quizRepository.deleteQuiz(quizId)
                .onSuccess {
                    _events.emit(MentorQuizEvent.ShowMessage("Quiz deleted successfully"))
                    load()
                }
                .onFailure {
                    _events.emit(MentorQuizEvent.ShowMessage(it.message ?: "Failed to delete quiz"))
                }
        }
    }
}
