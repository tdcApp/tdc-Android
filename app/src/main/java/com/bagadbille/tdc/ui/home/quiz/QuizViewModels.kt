package com.bagadbille.tdc.ui.home.quiz

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.model.Quiz
import com.bagadbille.tdc.data.model.QuizAnswer
import com.bagadbille.tdc.data.model.QuizQuestion
import com.bagadbille.tdc.data.model.QuizResult
import com.bagadbille.tdc.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// --- QuizList ---
sealed class QuizUiState {
    data object Loading : QuizUiState()
    data class Success(val quizzes: List<Quiz>) : QuizUiState()
    data class Error(val message: String) : QuizUiState()
}

@HiltViewModel
class QuizViewModel @Inject constructor(private val repo: QuizRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()
    init { loadQuizzes() }
    fun loadQuizzes() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            repo.getQuizzes()
                .onSuccess { _uiState.value = QuizUiState.Success(it) }
                .onFailure { _uiState.value = QuizUiState.Error(it.message ?: "Failed to load") }
        }
    }
}

// --- QuizTaking ---
sealed class QuizTakingUiState {
    data object Loading : QuizTakingUiState()
    data class InProgress(val questions: List<QuizQuestion>) : QuizTakingUiState()
    data object Submitting : QuizTakingUiState()
    data class Submitted(val isDisqualified: Boolean = false) : QuizTakingUiState()
    data class Error(val message: String) : QuizTakingUiState()
}

@HiltViewModel
class QuizTakingViewModel @Inject constructor(private val repo: QuizRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<QuizTakingUiState>(QuizTakingUiState.Loading)
    val uiState: StateFlow<QuizTakingUiState> = _uiState.asStateFlow()
    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()
    val selectedAnswers = mutableStateMapOf<String, String>()

    // Proctoring & Anti-Cheat State
    private val _warningCount = MutableStateFlow(0)
    val warningCount: StateFlow<Int> = _warningCount.asStateFlow()

    private val _activeWarning = MutableStateFlow<Int?>(null)
    val activeWarning: StateFlow<Int?> = _activeWarning.asStateFlow()

    var isInternalDialogActive: Boolean = false

    private var quizId = ""
    private var timerJob: Job? = null

    fun loadQuiz(id: String) {
        quizId = id
        viewModelScope.launch {
            _uiState.value = QuizTakingUiState.Loading
            repo.getQuizQuestions(id)
                .onSuccess { _uiState.value = QuizTakingUiState.InProgress(it); startTimer(30 * 60) }
                .onFailure { _uiState.value = QuizTakingUiState.Error(it.message ?: "Failed to load") }
        }
    }

    fun selectAnswer(questionId: String, option: String) {
        selectedAnswers[questionId] = option
    }

    fun getAnsweredCount() = selectedAnswers.count { it.value.isNotEmpty() }

    fun onAppExitOrFocusLost() {
        if (isInternalDialogActive) return
        if (_uiState.value !is QuizTakingUiState.InProgress) return
        if (_activeWarning.value != null) return

        val next = _warningCount.value + 1
        _warningCount.value = next

        when (next) {
            1 -> _activeWarning.value = 1
            2 -> _activeWarning.value = 2
            else -> {
                _activeWarning.value = 3
                submitQuiz(isDisqualified = true)
            }
        }
    }

    fun dismissWarning() {
        _activeWarning.value = null
    }

    fun submitQuiz(isDisqualified: Boolean = false) {
        timerJob?.cancel()
        viewModelScope.launch {
            _uiState.value = QuizTakingUiState.Submitting
            val answers = selectedAnswers.map { (qId, opt) -> QuizAnswer(qId, opt) }
            val clientId = UUID.randomUUID().toString()
            repo.submitQuiz(quizId, answers, clientId, isDisqualified = isDisqualified)
                .onSuccess { _uiState.value = QuizTakingUiState.Submitted(isDisqualified) }
                .onFailure { _uiState.value = QuizTakingUiState.Error(it.message ?: "Failed to submit") }
        }
    }

    private fun startTimer(totalSeconds: Int) {
        _timeRemaining.value = totalSeconds
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeRemaining.value > 0) { delay(1000); _timeRemaining.value -= 1 }
            submitQuiz()
        }
    }

    override fun onCleared() { super.onCleared(); timerJob?.cancel() }
}

// --- QuizResult ---
sealed class QuizResultUiState {
    data object Loading : QuizResultUiState()
    data class Success(val result: QuizResult) : QuizResultUiState()
    data class Error(val message: String) : QuizResultUiState()
}

@HiltViewModel
class QuizResultViewModel @Inject constructor(private val repo: QuizRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<QuizResultUiState>(QuizResultUiState.Loading)
    val uiState: StateFlow<QuizResultUiState> = _uiState.asStateFlow()
    fun loadResults(quizId: String) {
        viewModelScope.launch {
            _uiState.value = QuizResultUiState.Loading
            repo.getQuizResult(quizId)
                .onSuccess { _uiState.value = QuizResultUiState.Success(it) }
                .onFailure { _uiState.value = QuizResultUiState.Error(it.message ?: "Failed to load") }
        }
    }
}
