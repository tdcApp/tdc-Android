package com.bagadbille.tdc.ui.home.quiz

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.model.Quiz
import com.bagadbille.tdc.data.model.QuizAnswer
import com.bagadbille.tdc.data.model.QuizQuestion
import com.bagadbille.tdc.data.model.QuizResult
import com.bagadbille.tdc.data.remote.dto.QuizStartResponse
import com.bagadbille.tdc.data.remote.dto.QuizStrikeResponse
import com.bagadbille.tdc.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

// --- QuizList ---
sealed class QuizUiState {
    data object Loading : QuizUiState()
    data class Success(val quizzes: List<Quiz>, val isRefreshing: Boolean = false) : QuizUiState()
    data class Error(val message: String) : QuizUiState()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repo: QuizRepository,
    dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    val submittedQuizIds: StateFlow<Set<String>> = dataStoreManager.submittedQuizIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    init { loadQuizzes() }

    fun loadQuizzes(isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            val current = _uiState.value
            if (isPullToRefresh && current is QuizUiState.Success) {
                _uiState.value = current.copy(isRefreshing = true)
            } else if (!isPullToRefresh) {
                _uiState.value = QuizUiState.Loading
            }

            repo.getQuizzes()
                .onSuccess { _uiState.value = QuizUiState.Success(it, isRefreshing = false) }
                .onFailure {
                    if (current is QuizUiState.Success && isPullToRefresh) {
                        _uiState.value = current.copy(isRefreshing = false)
                    } else {
                        _uiState.value = QuizUiState.Error(it.message ?: "Failed to load")
                    }
                }
        }
    }
}

// --- QuizTaking ---
sealed class QuizTakingUiState {
    data object Loading : QuizTakingUiState()
    data class Confirmation(val quiz: Quiz?, val questions: List<QuizQuestion>) : QuizTakingUiState()
    data class InProgress(val questions: List<QuizQuestion>) : QuizTakingUiState()
    data object Submitting : QuizTakingUiState()
    data class Submitted(val isDisqualified: Boolean = false) : QuizTakingUiState()
    data class AlreadySubmitted(val quizId: String) : QuizTakingUiState()
    data class Error(val message: String) : QuizTakingUiState()
}

@HiltViewModel
class QuizTakingViewModel @Inject constructor(
    private val repo: QuizRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<QuizTakingUiState>(QuizTakingUiState.Loading)
    val uiState: StateFlow<QuizTakingUiState> = _uiState.asStateFlow()
    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()
    val selectedAnswers = mutableStateMapOf<String, String>()
    val markedForReview = mutableStateMapOf<String, Boolean>()

    val studentSeed: StateFlow<String> = dataStoreManager.firebaseUid
        .combine(dataStoreManager.userEmail) { uid, email ->
            uid?.ifBlank { null } ?: email?.ifBlank { null } ?: "student_default"
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "student_default")

    // Proctoring & Anti-Cheat State
    private val _warningCount = MutableStateFlow(0)
    val warningCount: StateFlow<Int> = _warningCount.asStateFlow()

    private val _activeWarning = MutableStateFlow<Int?>(null)
    val activeWarning: StateFlow<Int?> = _activeWarning.asStateFlow()

    var isInternalDialogActive: Boolean = false

    private var quizId = ""
    private var timerJob: Job? = null
    private var loadedQuestions: List<QuizQuestion> = emptyList()
    var loadedQuiz: Quiz? = null
        private set
    private var startedAtIso: String? = null
    private var startTimeMillis: Long = 0L

    fun loadQuiz(id: String) {
        quizId = id
        viewModelScope.launch {
            _uiState.value = QuizTakingUiState.Loading

            // Always fetch live quiz metadata, questions, and attempt status from DB
            repo.getQuizWithQuestions(id)
                .onSuccess { (quiz, questions) ->
                    if (quiz.isSubmitted) {
                        _uiState.value = QuizTakingUiState.AlreadySubmitted(id)
                        return@launch
                    }
                    loadedQuiz = quiz
                    val seedLong = (studentSeed.value + "_" + id).hashCode().toLong()
                    val orderedQuestions = questions.shuffled(java.util.Random(seedLong))
                    loadedQuestions = orderedQuestions

                    val active = quiz.activeAttempt
                    val localStartedAt = dataStoreManager.getActiveQuizStartedAt(id)
                    val localStrikes = dataStoreManager.getActiveQuizStrikes(id)

                    val isAttemptInProgress = (active != null && !active.isDisqualified) || (localStartedAt != null && localStartedAt > 0)

                    if (isAttemptInProgress) {
                        val effectiveStrikes = maxOf(active?.warningCount ?: 0, localStrikes)
                        _warningCount.value = effectiveStrikes

                        if (effectiveStrikes >= 3) {
                            submitQuiz(isDisqualified = true)
                            return@launch
                        }

                        // Restore any cached answers and review statuses
                        val savedAnswers = dataStoreManager.getSavedQuizAnswers(id, orderedQuestions.map { it.id })
                        savedAnswers.forEach { (qId, ans) ->
                            if (ans.isNotBlank()) {
                                selectedAnswers[qId] = ans
                            }
                        }
                        val savedReviews = dataStoreManager.getSavedQuizReviews(id, orderedQuestions.map { it.id })
                        savedReviews.forEach { qId ->
                            markedForReview[qId] = true
                        }

                        val totalDurationSec = (quiz.totalTime.takeIf { it > 0 } ?: 30) * 60
                        val startedMillis = if (active != null) {
                            try { java.time.Instant.parse(active.startedAt).toEpochMilli() } catch (_: Exception) { localStartedAt ?: System.currentTimeMillis() }
                        } else {
                            localStartedAt ?: System.currentTimeMillis()
                        }
                        startTimeMillis = startedMillis
                        startedAtIso = active?.startedAt ?: run {
                            val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                            isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                            isoFormat.format(java.util.Date(startedMillis))
                        }

                        val elapsedSec = Math.max(0L, (System.currentTimeMillis() - startedMillis) / 1000L).toInt()
                        val remainingSec = Math.max(0, totalDurationSec - elapsedSec)

                        if (remainingSec <= 0) {
                            submitQuiz(isDisqualified = false)
                            return@launch
                        }

                        // RESUME QUIZ: Cannot restart from question 1
                        _uiState.value = QuizTakingUiState.InProgress(orderedQuestions)
                        startTimerWithRemaining(remainingSec, totalDurationSec, startedMillis)

                        if (effectiveStrikes > 0) {
                            _activeWarning.value = effectiveStrikes
                        }
                    } else {
                        _uiState.value = QuizTakingUiState.Confirmation(quiz, orderedQuestions)
                    }
                }
                .onFailure {
                    if (repo.isQuizSubmittedLocally(id)) {
                        _uiState.value = QuizTakingUiState.AlreadySubmitted(id)
                    } else {
                        _uiState.value = QuizTakingUiState.Error(it.message ?: "Failed to load quiz")
                    }
                }
        }
    }

    fun startQuiz() {
        if (loadedQuestions.isNotEmpty()) {
            viewModelScope.launch {
                _uiState.value = QuizTakingUiState.Loading
                val durationMinutes = loadedQuiz?.totalTime?.takeIf { it > 0 } ?: 30
                val totalSeconds = durationMinutes * 60

                repo.startQuiz(quizId)
                    .onSuccess { response ->
                        if (response.isDisqualified || response.warningCount >= 3) {
                            _uiState.value = QuizTakingUiState.AlreadySubmitted(quizId)
                            return@launch
                        }
                        if (response.isExpired || (response.isResume && response.remainingSeconds <= 0)) {
                            _uiState.value = QuizTakingUiState.AlreadySubmitted(quizId)
                            return@launch
                        }

                        _warningCount.value = response.warningCount
                        startedAtIso = response.startedAt
                        startTimeMillis = if (response.startedAt != null) {
                            try { java.time.Instant.parse(response.startedAt).toEpochMilli() } catch (_: Exception) { System.currentTimeMillis() }
                        } else System.currentTimeMillis()

                        val remaining = if (response.isResume && response.remainingSeconds > 0) response.remainingSeconds else totalSeconds
                        _uiState.value = QuizTakingUiState.InProgress(loadedQuestions)
                        startTimerWithRemaining(remaining, totalSeconds, startTimeMillis)

                        if (response.isResume && response.warningCount > 0) {
                            _activeWarning.value = response.warningCount
                        }
                    }
                    .onFailure {
                        val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                        isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        startedAtIso = isoFormat.format(java.util.Date())
                        startTimeMillis = System.currentTimeMillis()
                        dataStoreManager.saveActiveQuiz(quizId, startTimeMillis, totalSeconds, 0)

                        _uiState.value = QuizTakingUiState.InProgress(loadedQuestions)
                        startTimerWithRemaining(totalSeconds, totalSeconds, startTimeMillis)
                    }
            }
        }
    }

    fun selectAnswer(questionId: String, option: String) {
        if (option.isBlank()) {
            selectedAnswers.remove(questionId)
        } else {
            selectedAnswers[questionId] = option
        }
        viewModelScope.launch {
            dataStoreManager.saveQuizAnswer(quizId, questionId, option)
        }
    }

    fun clearAnswer(questionId: String) {
        selectAnswer(questionId, "")
    }

    fun toggleMarkForReview(questionId: String) {
        val current = markedForReview[questionId] == true
        val next = !current
        if (next) {
            markedForReview[questionId] = true
        } else {
            markedForReview.remove(questionId)
        }
        viewModelScope.launch {
            dataStoreManager.saveQuizReviewStatus(quizId, questionId, next)
        }
    }

    fun getAnsweredCount() = selectedAnswers.count { it.value.isNotBlank() }
    fun getReviewCount() = markedForReview.count { it.value }
    fun getUnansweredCount(total: Int) = maxOf(0, total - getAnsweredCount())

    fun onAppExitOrFocusLost() {
        if (isInternalDialogActive) return
        if (_uiState.value !is QuizTakingUiState.InProgress) return
        if (_activeWarning.value != null) return

        val next = _warningCount.value + 1
        _warningCount.value = next

        viewModelScope.launch {
            repo.recordStrike(quizId)
        }

        when {
            next == 1 -> _activeWarning.value = 1
            next == 2 -> _activeWarning.value = 2
            else -> {
                _activeWarning.value = 3
                submitQuiz(isDisqualified = true)
            }
        }
    }

    fun exitWithStrike() {
        if (_uiState.value !is QuizTakingUiState.InProgress) return
        val next = _warningCount.value + 1
        _warningCount.value = next
        viewModelScope.launch {
            repo.recordStrike(quizId)
            if (next >= 3) {
                submitQuiz(isDisqualified = true)
            }
        }
    }

    fun dismissWarning() {
        _activeWarning.value = null
    }

    fun submitQuiz(isDisqualified: Boolean = false) {
        timerJob?.cancel()
        val timeTakenSeconds = if (startTimeMillis > 0) {
            ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt()
        } else null

        viewModelScope.launch {
            _uiState.value = QuizTakingUiState.Submitting
            val answers = selectedAnswers
                .filter { it.value.isNotBlank() }
                .map { (qId, opt) -> QuizAnswer(qId, opt) }
            val clientId = UUID.randomUUID().toString()
            repo.submitQuiz(
                quizId = quizId,
                answers = answers,
                clientSubmissionId = clientId,
                isDisqualified = isDisqualified,
                startedAt = startedAtIso,
                timeTakenSeconds = timeTakenSeconds
            )
                .onSuccess {
                    repo.markQuizSubmittedLocally(quizId)
                    _uiState.value = QuizTakingUiState.Submitted(isDisqualified)
                }
                .onFailure {
                    val msg = it.message ?: "Failed to submit"
                    if (msg.contains("already", ignoreCase = true) || msg.contains("409")) {
                        repo.markQuizSubmittedLocally(quizId)
                        _uiState.value = QuizTakingUiState.AlreadySubmitted(quizId)
                    } else {
                        _uiState.value = QuizTakingUiState.Error(msg)
                    }
                }
        }
    }

    private fun startTimerWithRemaining(remainingSeconds: Int, totalDurationSeconds: Int, startedAtMillis: Long) {
        _timeRemaining.value = remainingSeconds
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeRemaining.value > 0) {
                delay(1000)
                val elapsed = ((System.currentTimeMillis() - startedAtMillis) / 1000).toInt()
                val currentRemaining = maxOf(0, totalDurationSeconds - elapsed)
                _timeRemaining.value = currentRemaining
                if (currentRemaining <= 0) break
            }
            submitQuiz(isDisqualified = false)
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
class QuizResultViewModel @Inject constructor(
    private val repo: QuizRepository,
    dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<QuizResultUiState>(QuizResultUiState.Loading)
    val uiState: StateFlow<QuizResultUiState> = _uiState.asStateFlow()

    val userName: StateFlow<String> = combine(dataStoreManager.userName, dataStoreManager.userEmail) { name, email ->
        name?.ifBlank { null }
            ?: email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Student"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Student")

    val userEmail: StateFlow<String> = dataStoreManager.userEmail
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun loadResults(quizId: String) {
        viewModelScope.launch {
            _uiState.value = QuizResultUiState.Loading
            repo.getQuizResult(quizId)
                .onSuccess { _uiState.value = QuizResultUiState.Success(it) }
                .onFailure { _uiState.value = QuizResultUiState.Error(it.message ?: "Failed to load") }
        }
    }
}
