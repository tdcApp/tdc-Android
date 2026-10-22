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
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class CreateQuizUiState(
    val title: String = "",
    val audience: String = "all",
    val classId: String? = null,
    val targetYears: Set<String> = emptySet(),
    val classes: Map<String, String> = emptyMap(),
    val questions: List<QuizQuestionDraft> = emptyList(),
    val jsonFileName: String? = null,
    val isPreviewExpanded: Boolean = false,
    val startImmediately: Boolean = true,
    val startTimeIso: String? = null,
    val startTimeDisplay: String = "",
    val enteringWindowMinutes: Int = 15,
    val totalTimeMinutes: Int = 30,
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

    fun toggleTargetYear(year: String) {
        val current = _uiState.value.targetYears.toMutableSet()
        if (current.contains(year)) {
            current.remove(year)
        } else {
            current.add(year)
        }
        _uiState.value = _uiState.value.copy(targetYears = current, errorMessage = null)
    }

    fun selectAllYears() {
        _uiState.value = _uiState.value.copy(targetYears = emptySet(), errorMessage = null)
    }

    fun loadQuestionsFromJson(jsonContent: String, fileName: String? = null): Boolean {
        return try {
            val (parsedQuestions, parsedTitle) = parseQuestionsJson(jsonContent)
            _uiState.value = _uiState.value.copy(
                questions = parsedQuestions,
                jsonFileName = fileName ?: "Custom JSON",
                title = if (_uiState.value.title.isBlank() && !parsedTitle.isNullOrBlank()) parsedTitle else _uiState.value.title,
                errorMessage = null
            )
            viewModelScope.launch {
                _events.emit(CreateQuizEvent.ShowMessage("Loaded ${parsedQuestions.size} questions successfully!"))
            }
            true
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = e.message ?: "Failed to parse JSON questions"
            )
            false
        }
    }

    fun clearQuestions() {
        _uiState.value = _uiState.value.copy(
            questions = emptyList(),
            jsonFileName = null,
            isPreviewExpanded = false,
            errorMessage = null
        )
    }

    fun togglePreviewExpanded() {
        _uiState.value = _uiState.value.copy(
            isPreviewExpanded = !_uiState.value.isPreviewExpanded
        )
    }

    fun setErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun removeQuestion(index: Int) {
        val current = _uiState.value.questions.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.value = _uiState.value.copy(
                questions = current,
                errorMessage = null
            )
        }
    }

    fun updateQuestion(index: Int, draft: QuizQuestionDraft) {
        val current = _uiState.value.questions.toMutableList()
        if (index in current.indices) {
            current[index] = draft
            _uiState.value = _uiState.value.copy(questions = current, errorMessage = null)
        }
    }

    fun updateStartImmediately(immediate: Boolean) {
        _uiState.value = _uiState.value.copy(startImmediately = immediate, errorMessage = null)
    }

    fun updateStartTime(isoString: String?, displayString: String) {
        _uiState.value = _uiState.value.copy(
            startTimeIso = isoString,
            startTimeDisplay = displayString,
            errorMessage = null
        )
    }

    fun updateEnteringWindow(minutes: Int) {
        _uiState.value = _uiState.value.copy(
            enteringWindowMinutes = minutes.coerceAtLeast(1),
            errorMessage = null
        )
    }

    fun updateTotalTime(minutes: Int) {
        _uiState.value = _uiState.value.copy(
            totalTimeMinutes = minutes.coerceAtLeast(1),
            errorMessage = null
        )
    }

    fun saveQuiz() {
        val state = _uiState.value
        val title = state.title.trim()
        if (title.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter a quiz title")
            return
        }

        if (state.questions.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Please upload or paste a JSON file with questions")
            return
        }

        for (i in state.questions.indices) {
            val q = state.questions[i]
            if (q.question.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Question #${i + 1} prompt cannot be empty")
                return
            }
            if (q.optionA.isBlank() || q.optionB.isBlank() || q.optionC.isBlank() || q.optionD.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Question #${i + 1} has empty options")
                return
            }
            if (q.correctOption.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Question #${i + 1} has no correct option selected")
                return
            }
        }

        val targetYearsString = if (state.targetYears.isEmpty()) {
            null
        } else {
            state.targetYears.sorted().joinToString(",")
        }

        val finalStartTime = if (state.startImmediately) null else state.startTimeIso
        val finalEnteringWindow = state.enteringWindowMinutes.coerceAtLeast(1)
        val finalTotalTime = state.totalTimeMinutes.coerceAtLeast(1)

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            quizRepository.createQuiz(
                title = title,
                audience = state.audience,
                classId = state.classId,
                targetYears = targetYearsString,
                startTime = finalStartTime,
                enteringWindow = finalEnteringWindow,
                totalTime = finalTotalTime,
                questions = state.questions
            ).onSuccess {
                _events.emit(CreateQuizEvent.ShowMessage("Quiz created successfully!"))
                _events.emit(CreateQuizEvent.QuizSaved)
            }.onFailure {
                _uiState.value = state.copy(isSaving = false, errorMessage = it.message ?: "Failed to save quiz")
            }
        }
    }

    companion object {
        const val SAMPLE_QUIZ_JSON_TEMPLATE = """[
  {
    "question": "What is the capital of France?",
    "optionA": "Berlin",
    "optionB": "Madrid",
    "optionC": "Paris",
    "optionD": "Rome",
    "correctOption": "C"
  },
  {
    "question": "Which data type is immutable in Python?",
    "optionA": "List",
    "optionB": "Dictionary",
    "optionC": "Set",
    "optionD": "Tuple",
    "correctOption": "D"
  }
]"""

        fun parseQuestionsJson(jsonStr: String): Pair<List<QuizQuestionDraft>, String?> {
            val trimmed = jsonStr.trim()
            if (trimmed.isEmpty()) {
                throw IllegalArgumentException("JSON content is empty")
            }

            var optionalTitle: String? = null
            val jsonArray: JSONArray = try {
                if (trimmed.startsWith("[")) {
                    JSONArray(trimmed)
                } else if (trimmed.startsWith("{")) {
                    val rootObj = JSONObject(trimmed)
                    val t = rootObj.optString("title", "").trim()
                    if (t.isNotBlank()) {
                        optionalTitle = t
                    }
                    if (rootObj.has("questions")) {
                        rootObj.getJSONArray("questions")
                    } else {
                        throw IllegalArgumentException("JSON object must contain a 'questions' array: { \"questions\": [ ... ] }")
                    }
                } else {
                    throw IllegalArgumentException("Invalid format. Expected JSON array [ ... ] or object { \"questions\": [ ... ] }")
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Malformed JSON: ${e.message}")
            }

            if (jsonArray.length() == 0) {
                throw IllegalArgumentException("The JSON array does not contain any questions")
            }

            val extractedQuestions = mutableListOf<QuizQuestionDraft>()

            for (i in 0 until jsonArray.length()) {
                val qObj = jsonArray.optJSONObject(i)
                    ?: throw IllegalArgumentException("Item #${i + 1} is not a valid question object")

                val question = qObj.optString("question", "")
                    .ifBlank { qObj.optString("prompt", "") }
                    .ifBlank { qObj.optString("q", "") }
                    .trim()

                if (question.isBlank()) {
                    throw IllegalArgumentException("Question #${i + 1}: Question prompt is required and cannot be empty")
                }

                val optionA = qObj.optString("optionA", "")
                    .ifBlank { qObj.optString("option_a", "") }
                    .ifBlank { qObj.optString("a", "") }
                    .ifBlank { qObj.optString("A", "") }
                    .trim()

                val optionB = qObj.optString("optionB", "")
                    .ifBlank { qObj.optString("option_b", "") }
                    .ifBlank { qObj.optString("b", "") }
                    .ifBlank { qObj.optString("B", "") }
                    .trim()

                val optionC = qObj.optString("optionC", "")
                    .ifBlank { qObj.optString("option_c", "") }
                    .ifBlank { qObj.optString("c", "") }
                    .ifBlank { qObj.optString("C", "") }
                    .trim()

                val optionD = qObj.optString("optionD", "")
                    .ifBlank { qObj.optString("option_d", "") }
                    .ifBlank { qObj.optString("d", "") }
                    .ifBlank { qObj.optString("D", "") }
                    .trim()

                if (optionA.isBlank()) throw IllegalArgumentException("Question #${i + 1}: Option A is missing or blank")
                if (optionB.isBlank()) throw IllegalArgumentException("Question #${i + 1}: Option B is missing or blank")
                if (optionC.isBlank()) throw IllegalArgumentException("Question #${i + 1}: Option C is missing or blank")
                if (optionD.isBlank()) throw IllegalArgumentException("Question #${i + 1}: Option D is missing or blank")

                val rawCorrect = qObj.optString("correctOption", "")
                    .ifBlank { qObj.optString("correct_option", "") }
                    .ifBlank { qObj.optString("answer", "") }
                    .ifBlank { qObj.optString("correct", "") }
                    .trim()

                val normalizedCorrect = when (rawCorrect.uppercase()) {
                    "A", "1" -> "a"
                    "B", "2" -> "b"
                    "C", "3" -> "c"
                    "D", "4" -> "d"
                    optionA.uppercase() -> "a"
                    optionB.uppercase() -> "b"
                    optionC.uppercase() -> "c"
                    optionD.uppercase() -> "d"
                    else -> throw IllegalArgumentException(
                        "Question #${i + 1}: Invalid correctOption '$rawCorrect'. Must be A, B, C, or D"
                    )
                }

                extractedQuestions.add(
                    QuizQuestionDraft(
                        question = question,
                        optionA = optionA,
                        optionB = optionB,
                        optionC = optionC,
                        optionD = optionD,
                        correctOption = normalizedCorrect
                    )
                )
            }

            return Pair(extractedQuestions, optionalTitle)
        }
    }
}

