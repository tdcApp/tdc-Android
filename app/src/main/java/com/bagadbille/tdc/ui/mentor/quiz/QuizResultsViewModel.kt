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
    val submissions: List<QuizSubmissionDetail>,
    val isRefreshing: Boolean = false
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

    fun load(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            val current = _uiState.value
            if (isPullToRefresh && current is QuizResultsUiState.Success) {
                _uiState.value = QuizResultsUiState.Success(current.data.copy(isRefreshing = true))
            } else if (!isPullToRefresh) {
                _uiState.value = QuizResultsUiState.Loading
            }

            try {
                val quiz = quizRepository.getQuiz(quizId).getOrNull()
                    ?: quizRepository.getQuizzes().getOrDefault(emptyList()).firstOrNull { it.id == quizId }
                    ?: Quiz(id = quizId, title = "Quiz Results", audience = "all")

                val stats = quizRepository.getQuizStats(quizId).getOrDefault(
                    QuizStats(quizId, 0, 0.0, 0, 0)
                )
                val submissions = quizRepository.getQuizSubmissionsWithDetails(quizId)
                    .getOrDefault(emptyList())
                    .sortedWith(
                        compareByDescending<QuizSubmissionDetail> { it.submission.score }
                            .thenBy { it.submission.timeTakenSeconds ?: Int.MAX_VALUE }
                            .thenBy { it.submission.submittedAt }
                    )

                _uiState.value = QuizResultsUiState.Success(
                    QuizResultsUiData(
                        quiz = quiz,
                        stats = stats,
                        submissions = submissions,
                        isRefreshing = false
                    )
                )
            } catch (e: Exception) {
                if (isPullToRefresh && current is QuizResultsUiState.Success) {
                    _uiState.value = QuizResultsUiState.Success(current.data.copy(isRefreshing = false))
                } else {
                    _uiState.value = QuizResultsUiState.Error(e.message ?: "Failed to load quiz results")
                }
            }
        }
    }
}
