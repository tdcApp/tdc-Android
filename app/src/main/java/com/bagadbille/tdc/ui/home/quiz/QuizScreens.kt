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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
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
import com.bagadbille.tdc.data.model.QuizResultStatus
import com.bagadbille.tdc.data.model.QuizStatus
import com.bagadbille.tdc.ui.components.EmptyStateScreen
import com.bagadbille.tdc.ui.components.ErrorScreen
import com.bagadbille.tdc.ui.components.LoadingScreen
import com.bagadbille.tdc.ui.components.TdcButton
import kotlinx.coroutines.launch

// =============== QUIZ LIST ===============
@Composable
fun QuizListScreen(onNavigateToQuizTaking: (String) -> Unit, onNavigateToQuizResult: (String) -> Unit, viewModel: QuizViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val s = uiState) {
        is QuizUiState.Loading -> LoadingScreen()
        is QuizUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.loadQuizzes() })
        is QuizUiState.Success -> if (s.quizzes.isEmpty()) EmptyStateScreen(Icons.Outlined.Quiz, "No Quizzes", "No quizzes available right now.")
        else {
            val available = s.quizzes.filter { it.status == QuizStatus.AVAILABLE }
            val upcoming = s.quizzes.filter { it.status == QuizStatus.UPCOMING }
            val past = s.quizzes.filter { it.status == QuizStatus.COMPLETED || it.status == QuizStatus.SUBMITTED }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (available.isNotEmpty()) { item { Text("Available", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }; items(available, key = { it.id }) { QuizCard(it) { onNavigateToQuizTaking(it.id) } } }
                if (upcoming.isNotEmpty()) { item { Spacer(Modifier.height(8.dp)); Text("Upcoming", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }; items(upcoming, key = { it.id }) { QuizCard(it) { } } }
                if (past.isNotEmpty()) { item { Spacer(Modifier.height(8.dp)); Text("Past", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold) }; items(past, key = { it.id }) { QuizCard(it) { onNavigateToQuizResult(it.id) } } }
            }
        }
    }
}

@Composable
private fun QuizCard(quiz: Quiz, onClick: () -> Unit) {
    val (statusColor, statusText) = when (quiz.status) {
        QuizStatus.AVAILABLE -> MaterialTheme.colorScheme.primary to "Take Now"
        QuizStatus.UPCOMING -> MaterialTheme.colorScheme.secondary to "Upcoming"
        QuizStatus.COMPLETED -> MaterialTheme.colorScheme.onSurfaceVariant to "View Results"
        QuizStatus.SUBMITTED -> MaterialTheme.colorScheme.tertiary to "Submitted"
    }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(quiz.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Surface(shape = MaterialTheme.shapes.small, color = statusColor.copy(alpha = 0.15f)) { Text(statusText, style = MaterialTheme.typography.labelSmall, color = statusColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
            }
            Spacer(Modifier.height(4.dp)); Text(quiz.subject, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            quiz.description?.let { Spacer(Modifier.height(4.dp)); Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2) }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.HelpOutline, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(4.dp)); Text("${quiz.questionCount} Qs", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Timer, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.width(4.dp)); Text("${quiz.timeLimitMinutes} min", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
                is QuizTakingUiState.InProgress -> QuizContent(s.questions, viewModel.selectedAnswers, { qId, oId, multi -> viewModel.selectAnswer(qId, oId, multi) }, { showSubmitDialog = true })
                is QuizTakingUiState.Submitting -> LoadingScreen("Submitting...")
                is QuizTakingUiState.Submitted -> {}
            }
        }
    }
}

@Composable
private fun QuizContent(questions: List<QuizQuestion>, selectedAnswers: Map<String, List<String>>, onAnswerSelected: (String, String, Boolean) -> Unit, onSubmit: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { questions.size })
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        LinearProgressIndicator(progress = { (pagerState.currentPage + 1f) / questions.size }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
        Text("Question ${pagerState.currentPage + 1} of ${questions.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val q = questions[page]
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                if (q.isMultiAnswer) { Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)) { Text("Select all that apply", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }; Spacer(Modifier.height(12.dp)) }
                Text(q.questionText, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface); Spacer(Modifier.height(24.dp))
                val selected = selectedAnswers[q.id] ?: emptyList()
                q.options.forEach { opt ->
                    val isSel = opt.id in selected
                    Card(onClick = { onAnswerSelected(q.id, opt.id, q.isMultiAnswer) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = if (isSel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null, shape = MaterialTheme.shapes.medium) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (q.isMultiAnswer) Checkbox(isSel, { onAnswerSelected(q.id, opt.id, true) }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
                            else RadioButton(isSel, { onAnswerSelected(q.id, opt.id, false) }, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
                            Spacer(Modifier.width(12.dp)); Text(opt.text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
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
                is QuizResultUiState.Success -> if (s.result.status == QuizResultStatus.PENDING) PendingResult() else ReleasedResult(s.result)
            }
        }
    }
}

@Composable
private fun PendingResult() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Outlined.HourglassEmpty, null, Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            Spacer(Modifier.height(24.dp)); Text("Results Not Out Yet", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp)); Text("Your quiz has been submitted. Results will be available once released.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ReleasedResult(r: QuizResult) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), shape = MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your Score", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp)); Text("${r.score}/${r.totalScore}", style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    r.percentage?.let { Spacer(Modifier.height(4.dp)); Text("${"%.1f".format(it)}%", style = MaterialTheme.typography.headlineSmall, color = if (it >= 60) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
                }
            }
        }
        r.breakdown?.let { bd ->
            item { Spacer(Modifier.height(8.dp)); Text("Question Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(bd) { qr ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (qr.isCorrect) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.error.copy(alpha = 0.08f)), shape = MaterialTheme.shapes.medium) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (qr.isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel, null, tint = if (qr.isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp)); Text(qr.questionText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
