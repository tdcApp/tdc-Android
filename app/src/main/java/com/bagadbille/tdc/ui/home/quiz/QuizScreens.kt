package com.bagadbille.tdc.ui.home.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.model.Quiz
import com.bagadbille.tdc.data.model.QuizQuestion
import com.bagadbille.tdc.data.model.QuizResult
import com.bagadbille.tdc.ui.components.EmptyStateScreen
import com.bagadbille.tdc.ui.components.ErrorScreen
import com.bagadbille.tdc.ui.components.LoadingScreen
import com.bagadbille.tdc.ui.components.TdcButton
import kotlinx.coroutines.launch

// =============== QUIZ LIST ===============
@Composable
fun QuizListScreen(
    onNavigateToQuizTaking: (String) -> Unit,
    onNavigateToQuizResult: (String) -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val s = uiState) {
        is QuizUiState.Loading -> LoadingScreen()
        is QuizUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.loadQuizzes() })
        is QuizUiState.Success -> {
            if (s.quizzes.isEmpty()) {
                EmptyStateScreen(Icons.Outlined.Quiz, "No Quizzes", "No quizzes available right now.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Available Quizzes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "${s.quizzes.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    items(s.quizzes, key = { it.id }) { quiz ->
                        QuizCard(quiz) { onNavigateToQuizTaking(quiz.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizCard(quiz: Quiz, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
            ) {
                Icon(
                    Icons.Outlined.Quiz,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(24.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    quiz.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            quiz.audience.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    quiz.createdAt?.let {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Outlined.AccessTime,
                            null,
                            Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            it.take(10),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    "Start",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// =============== QUIZ TAKING ===============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTakingScreen(quizId: String, onNavigateBack: () -> Unit, onQuizSubmitted: () -> Unit, viewModel: QuizTakingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timeRemaining by viewModel.timeRemaining.collectAsStateWithLifecycle()
    var showSubmitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(quizId) { viewModel.loadQuiz(quizId) }
    LaunchedEffect(uiState) { if (uiState is QuizTakingUiState.Submitted) onQuizSubmitted() }

    if (showSubmitDialog) {
        AlertDialog(onDismissRequest = { showSubmitDialog = false }, title = { Text("Submit Quiz?") },
            text = { val ac = viewModel.getAnsweredCount(); val tc = (uiState as? QuizTakingUiState.InProgress)?.questions?.size ?: 0; Text("You have answered $ac out of $tc questions.") },
            confirmButton = { TextButton(onClick = { showSubmitDialog = false; viewModel.submitQuiz() }) { Text("Submit", color = MaterialTheme.colorScheme.primary) } },
            dismissButton = { TextButton(onClick = { showSubmitDialog = false }) { Text("Cancel") } })
    }

    Scaffold(topBar = {
        TopAppBar(title = { Column { Text("Quiz", style = MaterialTheme.typography.titleMedium); Text(formatTime(timeRemaining), style = MaterialTheme.typography.labelMedium, color = if (timeRemaining < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) } },
            navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            actions = { TextButton(onClick = { showSubmitDialog = true }) { Text("Submit", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface))
    }) { ip ->
        Box(Modifier.padding(ip)) {
            when (val s = uiState) {
                is QuizTakingUiState.Loading -> LoadingScreen("Loading quiz...")
                is QuizTakingUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.loadQuiz(quizId) })
                is QuizTakingUiState.InProgress -> QuizContent(s.questions, viewModel.selectedAnswers, { qId, opt -> viewModel.selectAnswer(qId, opt) }, { showSubmitDialog = true })
                is QuizTakingUiState.Submitting -> LoadingScreen("Submitting...")
                is QuizTakingUiState.Submitted -> {}
            }
        }
    }
}

@Composable
private fun QuizContent(questions: List<QuizQuestion>, selectedAnswers: Map<String, String>, onAnswerSelected: (String, String) -> Unit, onSubmit: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { questions.size })
    val scope = rememberCoroutineScope()
    val options = listOf("a", "b", "c", "d")
    Column(Modifier.fillMaxSize()) {
        LinearProgressIndicator(progress = { (pagerState.currentPage + 1f) / questions.size }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
        Text("Question ${pagerState.currentPage + 1} of ${questions.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val q = questions[page]
            val optionTexts = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                Text(q.question, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface); Spacer(Modifier.height(24.dp))
                val selected = selectedAnswers[q.id] ?: ""
                options.forEachIndexed { idx, opt ->
                    val isSel = selected == opt
                    Card(onClick = { onAnswerSelected(q.id, opt) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = if (isSel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null, shape = MaterialTheme.shapes.medium) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(isSel, { onAnswerSelected(q.id, opt) }, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
                            Spacer(Modifier.width(12.dp)); Text(optionTexts[idx], style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            if (pagerState.currentPage > 0) OutlinedButton(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }, shape = MaterialTheme.shapes.medium) { Text("Previous") } else Spacer(Modifier.width(1.dp))
            if (pagerState.currentPage < questions.size - 1) Button(onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }, shape = MaterialTheme.shapes.medium) { Text("Next") }
            else TdcButton("Submit Quiz", onSubmit, modifier = Modifier.width(160.dp))
        }
    }
}

private fun formatTime(seconds: Int): String { val m = seconds / 60; val s = seconds % 60; return "%02d:%02d".format(m, s) }

// =============== QUIZ RESULT ===============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(quizId: String, onNavigateBack: () -> Unit, viewModel: QuizResultViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(quizId) { viewModel.loadResults(quizId) }
    Scaffold(topBar = { TopAppBar(title = { Text("Quiz Results") }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) }) { ip ->
        Box(Modifier.padding(ip)) {
            when (val s = uiState) {
                is QuizResultUiState.Loading -> LoadingScreen("Loading results...")
                is QuizResultUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.loadResults(quizId) })
                is QuizResultUiState.Success -> ResultContent(s.result)
            }
        }
    }
}

@Composable
private fun ResultContent(r: QuizResult) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(Modifier.fillMaxWidth().padding(32.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Your Score", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text("${r.score}/${r.totalQuestions}", style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                val pct = if (r.totalQuestions > 0) (r.score * 100f / r.totalQuestions) else 0f
                Text("${"%.0f".format(pct)}%", style = MaterialTheme.typography.headlineSmall, color = if (pct >= 60) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                r.submittedAt?.let {
                    Spacer(Modifier.height(16.dp))
                    Text("Submitted: ${it.take(10)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
