package com.bagadbille.tdc.ui.home.quiz

import android.app.Activity
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.model.Quiz
import com.bagadbille.tdc.data.model.QuizQuestion
import com.bagadbille.tdc.data.model.QuizResult
import com.bagadbille.tdc.ui.components.CyberCard
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
    val submittedQuizIds by viewModel.submittedQuizIds.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is QuizUiState.Loading -> LoadingScreen()
        is QuizUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.loadQuizzes() })
        is QuizUiState.Success -> {
            com.bagadbille.tdc.ui.components.TdcPullToRefresh(
                isRefreshing = s.isRefreshing,
                onRefresh = { viewModel.loadQuizzes(isPullToRefresh = true) }
            ) {
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
                            val isCompleted = quiz.isSubmitted || quiz.id in submittedQuizIds
                            QuizCard(
                                quiz = quiz,
                                isCompleted = isCompleted,
                                onClick = {
                                    if (isCompleted) {
                                        onNavigateToQuizResult(quiz.id)
                                    } else {
                                        onNavigateToQuizTaking(quiz.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizCard(
    quiz: Quiz,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    CyberCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                color = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
            ) {
                Icon(
                    if (isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.Quiz,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(24.dp),
                    tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
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
                    quiz.targetYears?.let { years ->
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                years,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
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

                // Creator name: ADMIN or Teacher name
                val creator = quiz.createdByName?.ifBlank { null }
                if (!creator.isNullOrBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            "By $creator",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            fontWeight = if (creator == "ADMIN") FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))
            if (isCompleted) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Completed",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        "Start",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// =============== QUIZ TAKING (PROCTORED ENVIRONMENT) ===============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTakingScreen(
    quizId: String,
    onNavigateBack: () -> Unit,
    onQuizSubmitted: (Boolean) -> Unit,
    viewModel: QuizTakingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timeRemaining by viewModel.timeRemaining.collectAsStateWithLifecycle()
    val warningCount by viewModel.warningCount.collectAsStateWithLifecycle()
    val activeWarning by viewModel.activeWarning.collectAsStateWithLifecycle()

    var showSubmitDialog by remember { mutableStateOf(false) }
    var showBackAttemptDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isQuizActive = uiState is QuizTakingUiState.InProgress

    // 1. Hardware / Window Security Lockdown: FLAG_SECURE + Immersive Sticky Fullscreen (Only when quiz is active)
    DisposableEffect(activity, isQuizActive) {
        if (isQuizActive) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            val insetsController = activity?.let { WindowCompat.getInsetsController(it.window, it.window.decorView) }
            insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            val insetsController = activity?.let { WindowCompat.getInsetsController(it.window, it.window.decorView) }
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // 2. Lifecycle Detection: Detects app minimized, home pressed, task switched (Only when quiz is active)
    DisposableEffect(lifecycleOwner, isQuizActive) {
        val observer = LifecycleEventObserver { _, event ->
            if (isQuizActive && (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP)) {
                viewModel.onAppExitOrFocusLost()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 3. Window Focus Loss Detection: Detects split-screen, notification shade, floating windows (Only when quiz is active)
    DisposableEffect(view, isQuizActive) {
        val listener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (isQuizActive && !hasFocus) {
                viewModel.onAppExitOrFocusLost()
            }
        }
        view.viewTreeObserver.addOnWindowFocusChangeListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
        }
    }

    // 4. Intercept System Back Gestures
    BackHandler(enabled = true) {
        if (isQuizActive) {
            viewModel.isInternalDialogActive = true
            showBackAttemptDialog = true
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(quizId) { viewModel.loadQuiz(quizId) }
    LaunchedEffect(uiState) {
        if (uiState is QuizTakingUiState.Submitted) {
            val isDisq = (uiState as QuizTakingUiState.Submitted).isDisqualified
            onQuizSubmitted(isDisq)
        }
    }

    // Back Attempt Warning Dialog
    if (showBackAttemptDialog) {
        AlertDialog(
            onDismissRequest = {
                showBackAttemptDialog = false
                viewModel.isInternalDialogActive = false
            },
            icon = { Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp)) },
            title = { Text("Leaving Quiz Forbidden", fontWeight = FontWeight.Bold) },
            text = {
                Text("You cannot leave or navigate back during the quiz. Exiting the app will record an integrity strike.\n\n3 strikes will immediately terminate, auto-submit, and disqualify your test.")
            },
            confirmButton = {
                Button(onClick = {
                    showBackAttemptDialog = false
                    viewModel.isInternalDialogActive = false
                }) {
                    Text("Stay in Quiz")
                }
            }
        )
    }

    // Strike 1 Warning Dialog
    if (activeWarning == 1) {
        AlertDialog(
            onDismissRequest = { /* Must acknowledge */ },
            icon = { Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(40.dp)) },
            title = { Text("⚠️ Warning 1 of 3: App Exit Detected", fontWeight = FontWeight.Bold) },
            text = {
                Text("You navigated away from or minimized the quiz window. Leaving the quiz screen is strictly prohibited.\n\nYou have 2 warnings remaining before your test is automatically terminated.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissWarning() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("I Understand & Resume Test")
                }
            }
        )
    }

    // Strike 2 Warning Dialog (Final Warning)
    if (activeWarning == 2) {
        AlertDialog(
            onDismissRequest = { /* Must acknowledge */ },
            icon = { Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(44.dp)) },
            title = { Text("🚨 FINAL WARNING (2 of 3)!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = {
                Text("You exited or switched away from the quiz again!\n\nTHIS IS YOUR FINAL WARNING. If you leave the quiz or switch windows one more time, your test will be immediately terminated, submitted, and disqualified.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissWarning() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Return to Test (Final Chance)")
                }
            }
        )
    }

    // Manual Submit Confirmation Dialog
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = {
                showSubmitDialog = false
                viewModel.isInternalDialogActive = false
            },
            title = { Text("Submit Quiz?") },
            text = {
                val ac = viewModel.getAnsweredCount()
                val tc = (uiState as? QuizTakingUiState.InProgress)?.questions?.size ?: 0
                Text("You have answered $ac out of $tc questions. Are you sure you want to finish and submit your responses?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showSubmitDialog = false
                    viewModel.isInternalDialogActive = false
                    viewModel.submitQuiz()
                }) {
                    Text("Submit", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSubmitDialog = false
                    viewModel.isInternalDialogActive = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    when (val s = uiState) {
        is QuizTakingUiState.Loading -> LoadingScreen("Preparing test environment...")
        is QuizTakingUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.loadQuiz(quizId) })
        is QuizTakingUiState.Confirmation -> QuizConfirmationContent(
            quiz = s.quiz,
            questionCount = s.questions.size,
            onConfirm = { viewModel.startQuiz() },
            onCancel = onNavigateBack
        )
        is QuizTakingUiState.InProgress -> {
            Scaffold(topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Proctored Quiz", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    formatTime(timeRemaining),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (timeRemaining < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }

                            // Warning Strike Badge
                            val strikeColor = when (warningCount) {
                                0 -> MaterialTheme.colorScheme.primary
                                1 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = strikeColor.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, strikeColor.copy(alpha = 0.35f)),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Outlined.Shield, null, tint = strikeColor, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Strikes: $warningCount/3",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = strikeColor
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.isInternalDialogActive = true
                            showBackAttemptDialog = true
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            viewModel.isInternalDialogActive = true
                            showSubmitDialog = true
                        }) {
                            Text("Submit", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }) { ip ->
                Box(Modifier.padding(ip)) {
                    QuizContent(
                        s.questions,
                        viewModel.selectedAnswers,
                        { qId, opt -> viewModel.selectAnswer(qId, opt) },
                        {
                            viewModel.isInternalDialogActive = true
                            showSubmitDialog = true
                        }
                    )
                }
            }
        }
        is QuizTakingUiState.Submitting -> LoadingScreen("Encrypting and submitting...")
        is QuizTakingUiState.Submitted -> {}
        is QuizTakingUiState.AlreadySubmitted -> {
            QuizAlreadySubmittedContent(
                quizId = s.quizId,
                onViewResult = { onQuizSubmitted(false) },
                onBack = onNavigateBack
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizAlreadySubmittedContent(
    quizId: String,
    onViewResult: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Status", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Quiz Already Submitted",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "You have already submitted this quiz once. Each student is allowed only a single attempt.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    TdcButton(
                        text = "View Submission Status",
                        onClick = onViewResult,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Quizzes")
                    }
                }
            }
        }
    }
}

// =============== PRE-EXAM BRIEFING & CONFIRMATION ===============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizConfirmationContent(
    quiz: Quiz?,
    questionCount: Int,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pre-Exam Briefing", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Security,
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Entering Quiz Environment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "This quiz operates under strict anti-cheat proctoring. Please review the environment rules before proceeding.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // Quiz Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        quiz?.title ?: "Special Selection Quiz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "📝 $questionCount Questions",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "⏱️ 30 Minutes",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "🛡️ Monitored",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Rules / Proctoring Checklist
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Proctoring Rules & Policy",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    RuleItem(
                        icon = Icons.Outlined.Warning,
                        iconTint = MaterialTheme.colorScheme.error,
                        title = "3-Strike App Exit Warning",
                        description = "Do not exit, minimize, or switch apps. 1st exit gives Warning 1, 2nd exit gives Final Warning. On the 3rd exit, your quiz will automatically terminate and submit as disqualified."
                    )

                    RuleItem(
                        icon = Icons.Outlined.Lock,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Screen Security Lockdown",
                        description = "Taking screenshots, screen recordings, or casting is strictly blocked for exam security."
                    )

                    RuleItem(
                        icon = Icons.Outlined.Fullscreen,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = "Full-Screen Mode",
                        description = "The test will occupy the entire screen. Navigation gestures and system back buttons are locked until submission."
                    )

                    RuleItem(
                        icon = Icons.Outlined.VisibilityOff,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = "Confidential Results",
                        description = "Scores will not be displayed to candidates upon completion. Responses are submitted directly to the administration for Top 50–70 ranking."
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Outlined.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("I Understand & Start Quiz", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Cancel & Go Back")
            }
        }
    }
}

@Composable
private fun RuleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = iconTint.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(18.dp), tint = iconTint)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

// =============== QUIZ RESULT (CONFIDENTIAL FOR STUDENTS) ===============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    quizId: String,
    isDisqualified: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: QuizResultViewModel = hiltViewModel()
) {
    val studentName by viewModel.userName.collectAsStateWithLifecycle()
    val studentEmail by viewModel.userEmail.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Status", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { ip ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(ip)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isDisqualified) MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isDisqualified) {
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Text(
                            "Quiz Terminated",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "Disqualified (3 Warnings Exceeded)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Text(
                            "Quiz Submitted",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "Responses Recorded Successfully",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Candidate Identity Block
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    studentName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (studentEmail.isNotBlank()) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        studentEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = if (isDisqualified) MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "Attempt 1/1",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDisqualified) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            if (isDisqualified) {
                                "Your test was automatically submitted because you navigated away or exited the application 3 times.\n\nYour responses have been recorded and flagged for administrative review."
                            } else {
                                "Your quiz responses have been submitted for evaluation.\n\nNote: Individual scores are confidential and only visible to the administration.\n\nShortlisted candidates (Top 50–70) will be contacted directly after evaluation."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    TdcButton(
                        text = "Return to Home",
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
