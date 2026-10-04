package com.bagadbille.tdc.ui.mentor.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Quiz
import com.bagadbille.tdc.data.model.QuizStats
import com.bagadbille.tdc.data.model.QuizSubmissionDetail
import com.bagadbille.tdc.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizResultsUiData(
    val quiz: Quiz,
    val stats: QuizStats,
    val submissions: List<QuizSubmissionDetail>
)

sealed class QuizResultsUiState {
    data object Loading : QuizResultsUiState()
    data class Success(val data: QuizResultsUiData) : QuizResultsUiState()
    data class Error(val message: String) : QuizResultsUiState()
}

@HiltViewModel
class QuizResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository
) : ViewModel() {

    val quizId: String = checkNotNull(savedStateHandle["quizId"])

    private val _uiState = MutableStateFlow<QuizResultsUiState>(QuizResultsUiState.Loading)
    val uiState: StateFlow<QuizResultsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = QuizResultsUiState.Loading
            try {
                val quizzes = quizRepository.getQuizzes().getOrDefault(emptyList())
                val quiz = quizzes.firstOrNull { it.id == quizId }
                    ?: throw IllegalArgumentException("Quiz not found")
                val stats = quizRepository.getQuizStats(quizId).getOrDefault(
                    QuizStats(quizId, 0, 0.0, 0, 0)
                )
                val submissions = quizRepository.getQuizSubmissionsWithDetails(quizId).getOrDefault(emptyList())

                _uiState.value = QuizResultsUiState.Success(
                    QuizResultsUiData(
                        quiz = quiz,
                        stats = stats,
                        submissions = submissions
                    )
                )
            } catch (e: Exception) {
                _uiState.value = QuizResultsUiState.Error(e.message ?: "Failed to load quiz results")
            }
        }
    }
}
