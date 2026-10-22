package com.bagadbille.tdc.ui.home.quiz

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import com.bagadbille.tdc.ui.theme.BrandCyan
import com.bagadbille.tdc.ui.theme.BrandOrange
import com.bagadbille.tdc.ui.theme.DarkBackground
import kotlinx.coroutines.delay
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
                            val isSpecialAdminQuiz = quiz.creatorRole?.equals("ADMIN", ignoreCase = true) == true ||
                                                     quiz.title.contains("Coding Quest", ignoreCase = true) ||
                                                     quiz.title.contains("Quest", ignoreCase = true)

                            if (isSpecialAdminQuiz) {
                                SpecialAdminQuizCard(
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
                            } else {
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
}

@Composable
private fun SpecialAdminQuizCard(
    quiz: Quiz,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val isComingSoon = quiz.questionCount == 0 || quiz.title.contains("Coming Soon", ignoreCase = true)
    val formUrl = "https://forms.gle/nkSmmE9EU31os5UU8"

    CyberCard(
        onClick = {
            if (isComingSoon) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formUrl))
                context.startActivity(intent)
            } else {
                onClick()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        backgroundBrush = if (isDark) {
            Brush.linearGradient(
                listOf(Color(0xFF0F2B33), Color(0xFF2C1910), Color(0xFF17191E))
            )
        } else {
            Brush.linearGradient(
                listOf(Color(0xFFF0FDFA), Color(0xFFFFF7ED), Color(0xFFFFFFFF))
            )
        },
        borderBrush = Brush.horizontalGradient(
            listOf(
                BrandCyan.copy(alpha = if (isDark) 0.85f else 0.6f),
                BrandOrange.copy(alpha = if (isDark) 0.85f else 0.6f)
            )
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) BrandOrange.copy(alpha = 0.22f) else Color(0xFFFFEDD5))
                        .border(1.dp, if (isDark) BrandOrange.copy(alpha = 0.5f) else Color(0xFFFDBA74), RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.WorkspacePremium,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isDark) BrandOrange else Color(0xFFC2410C)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "CODING QUEST SEASON 3",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = if (isDark) BrandOrange else Color(0xFFC2410C)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isCompleted -> BrandCyan.copy(alpha = 0.18f)
                        quiz.hasActiveAttempt -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f)
                        isComingSoon -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                        else -> BrandOrange.copy(alpha = 0.2f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isCompleted -> BrandCyan.copy(alpha = 0.45f)
                            quiz.hasActiveAttempt -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                            isComingSoon -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)
                            else -> BrandOrange.copy(alpha = 0.6f)
                        }
                    )
                ) {
                    Text(
                        text = when {
                            isCompleted -> "COMPLETED"
                            quiz.hasActiveAttempt -> "⚡ IN PROGRESS"
                            isComingSoon -> "COMING SOON"
                            else -> "⚡ LIVE NOW"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when {
                            isCompleted -> BrandCyan
                            quiz.hasActiveAttempt -> MaterialTheme.colorScheme.tertiary
                            isComingSoon -> MaterialTheme.colorScheme.tertiary
                            else -> BrandOrange
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Title
            Text(
                text = quiz.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                ),
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )

            Spacer(Modifier.height(8.dp))

            // Metadata Row: Audience, Year, Questions count, Admin Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = BrandCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        quiz.audience.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                quiz.targetYears?.let { years ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = BrandOrange.copy(alpha = 0.15f)
                    ) {
                        Text(
                            years,
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (quiz.questionCount > 0) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "${quiz.questionCount} Questions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, BrandOrange.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👑 ADMIN", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = BrandOrange)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Action Button
            when {
                isCompleted -> {
                    TdcButton(
                        text = "View Result",
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                isComingSoon -> {
                    TdcButton(
                        text = "Register for Season 3",
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    TdcButton(
                        text = if (quiz.hasActiveAttempt) "Resume Quiz Now" else "Join Challenge Now",
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        if (quiz.hasActiveAttempt) "Resume" else "Start",
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
    val studentSeed by viewModel.studentSeed.collectAsStateWithLifecycle()

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

    val coroutineScope = rememberCoroutineScope()

    // 2. Lifecycle Detection: Detects app minimized, home pressed, task switched (Only when quiz is active)
    DisposableEffect(lifecycleOwner, isQuizActive) {
        val observer = LifecycleEventObserver { _, event ->
            if (isQuizActive && (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP)) {
                if (activity?.isChangingConfigurations == true) return@LifecycleEventObserver
                if (viewModel.isInternalDialogActive) return@LifecycleEventObserver
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
        var focusLossJob: kotlinx.coroutines.Job? = null
        val listener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) {
                focusLossJob?.cancel()
                focusLossJob = null
            } else if (isQuizActive) {
                if (activity?.isChangingConfigurations == true) return@OnWindowFocusChangeListener
                if (viewModel.isInternalDialogActive) return@OnWindowFocusChangeListener
                focusLossJob?.cancel()
                focusLossJob = coroutineScope.launch {
                    delay(800)
                    if (isQuizActive && !view.hasWindowFocus()) {
                        if (activity?.isChangingConfigurations == true) return@launch
                        if (viewModel.isInternalDialogActive) return@launch
                        viewModel.onAppExitOrFocusLost()
                    }
                }
            }
        }
        view.viewTreeObserver.addOnWindowFocusChangeListener(listener)
        onDispose {
            focusLossJob?.cancel()
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
                coroutineScope.launch {
                    delay(300)
                    viewModel.isInternalDialogActive = false
                }
            },
            icon = { Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp)) },
            title = { Text("Leaving Quiz Forbidden", fontWeight = FontWeight.Bold) },
            text = {
                Text("You cannot leave or navigate back during the quiz. Exiting the app will record an integrity strike.\n\n3 strikes will immediately terminate, auto-submit, and disqualify your test.")
            },
            confirmButton = {
                Button(onClick = {
                    showBackAttemptDialog = false
                    coroutineScope.launch {
                        delay(300)
                        viewModel.isInternalDialogActive = false
                    }
                }) {
                    Text("Stay in Quiz")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackAttemptDialog = false
                    viewModel.isInternalDialogActive = false
                    viewModel.exitWithStrike()
                    onNavigateBack()
                }) {
                    Text("Exit Quiz (Strike +1)", color = MaterialTheme.colorScheme.error)
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
        val total = (uiState as? QuizTakingUiState.InProgress)?.questions?.size ?: 0
        val answered = viewModel.getAnsweredCount()
        val review = viewModel.getReviewCount()
        val unanswered = viewModel.getUnansweredCount(total)

        AlertDialog(
            onDismissRequest = {
                showSubmitDialog = false
                coroutineScope.launch {
                    delay(300)
                    viewModel.isInternalDialogActive = false
                }
            },
            icon = {
                Icon(
                    Icons.Outlined.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("Submit Quiz?", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Are you sure you want to finish and submit your quiz?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Answered", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("$answered / $total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Marked for Review", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("$review", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Unanswered", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("$unanswered", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (unanswered > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    if (unanswered > 0 || review > 0) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Notice: You have $unanswered unanswered questions and $review marked for review. Answers cannot be modified after submitting.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        viewModel.isInternalDialogActive = false
                        viewModel.submitQuiz()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Confirm Submit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSubmitDialog = false
                        coroutineScope.launch {
                            delay(300)
                            viewModel.isInternalDialogActive = false
                        }
                    }
                ) {
                    Text("Continue Quiz")
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
            QuizContent(
                questions = s.questions,
                selectedAnswers = viewModel.selectedAnswers,
                markedForReview = viewModel.markedForReview,
                timeRemaining = timeRemaining,
                warningCount = warningCount,
                studentSeed = studentSeed,
                onAnswerSelected = { qId, opt -> viewModel.selectAnswer(qId, opt) },
                onToggleMarkForReview = { qId -> viewModel.toggleMarkForReview(qId) },
                onFloaterStateChanged = { isOpen ->
                    viewModel.isInternalDialogActive = isOpen
                },
                onSubmit = {
                    viewModel.isInternalDialogActive = true
                    showSubmitDialog = true
                }
            )
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
            val isDark = MaterialTheme.colorScheme.background == DarkBackground
            val isSpecialAdminQuiz = quiz?.creatorRole?.equals("ADMIN", ignoreCase = true) == true ||
                                     quiz?.title?.contains("Coding Quest", ignoreCase = true) == true ||
                                     quiz?.title?.contains("Quest", ignoreCase = true) == true

            if (isSpecialAdminQuiz) {
                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundBrush = if (isDark) {
                        Brush.linearGradient(
                            listOf(Color(0xFF0F2B33), Color(0xFF2C1910), Color(0xFF17191E))
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(Color(0xFFF0FDFA), Color(0xFFFFF7ED), Color(0xFFFFFFFF))
                        )
                    },
                    borderBrush = Brush.horizontalGradient(
                        listOf(
                            BrandCyan.copy(alpha = if (isDark) 0.85f else 0.6f),
                            BrandOrange.copy(alpha = if (isDark) 0.85f else 0.6f)
                        )
                    )
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) BrandOrange.copy(alpha = 0.22f) else Color(0xFFFFEDD5))
                                    .border(1.dp, if (isDark) BrandOrange.copy(alpha = 0.5f) else Color(0xFFFDBA74), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.WorkspacePremium,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = if (isDark) BrandOrange else Color(0xFFC2410C)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "⚡ TDC CODING QUEST SEASON 3",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.8.sp
                                        ),
                                        color = if (isDark) BrandOrange else Color(0xFFC2410C)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BrandCyan.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    "OFFICIAL CONTEST",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BrandCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            quiz?.title ?: "TDC Coding Quest Season 3",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = BrandCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "📝 $questionCount Questions",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = BrandOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "⏱️ ${quiz?.totalTime ?: 30} Minutes",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandOrange,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    "🛡️ Anti-Cheat Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            } else {
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
                                    "⏱️ ${quiz?.totalTime ?: 30} Minutes",
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

            if (isSpecialAdminQuiz) {
                TdcButton(
                    text = "Enter Coding Quest Challenge",
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                )
            } else {
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

private data class ShuffledQuizOption(
    val originalKey: String,
    val displayBadge: String,
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizContent(
    questions: List<QuizQuestion>,
    selectedAnswers: Map<String, String>,
    markedForReview: Map<String, Boolean>,
    timeRemaining: Int,
    warningCount: Int,
    studentSeed: String = "",
    onAnswerSelected: (String, String) -> Unit,
    onToggleMarkForReview: (String) -> Unit,
    onFloaterStateChanged: (Boolean) -> Unit = {},
    onSubmit: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { questions.size })
    val scope = rememberCoroutineScope()
    val questionStripListState = rememberLazyListState()
    var showPaletteSheet by remember { mutableStateOf(false) }

    // Keep horizontal strip centered with active pager question
    LaunchedEffect(pagerState.currentPage) {
        questionStripListState.animateScrollToItem(maxOf(0, pagerState.currentPage - 2))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ==================== 1. TOP BAR ====================
            // Left: Timer | Center: Strikes | Right: End/Submit
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // TOP LEFT: Live Countdown Timer
                    val isUrgent = timeRemaining < 300 // under 5 minutes
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isUrgent) MaterialTheme.colorScheme.error.copy(alpha = 0.14f) else BrandCyan.copy(alpha = 0.12f),
                        border = BorderStroke(
                            1.dp,
                            if (isUrgent) MaterialTheme.colorScheme.error.copy(alpha = 0.6f) else BrandCyan.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = "Time Remaining",
                                modifier = Modifier.size(15.dp),
                                tint = if (isUrgent) MaterialTheme.colorScheme.error else BrandCyan
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = formatTime(timeRemaining),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // TOP CENTER: Strikes Badge
                    val strikeColor = when (warningCount) {
                        0 -> BrandCyan
                        1 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = strikeColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, strikeColor.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (warningCount > 0) Icons.Outlined.WarningAmber else Icons.Outlined.Shield,
                                contentDescription = "Strikes",
                                tint = strikeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "Strikes: $warningCount/3",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = strikeColor
                            )
                        }
                    }

                    // TOP RIGHT: End / Submit Button
                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "End / Submit",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // ==================== 2. QUESTION STRIP & EXPANDABLE FLOATER ====================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Horizontal scrollable question pills
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        state = questionStripListState
                    ) {
                        itemsIndexed(questions) { index, q ->
                            val isCurrent = pagerState.currentPage == index
                            val isAnswered = !selectedAnswers[q.id].isNullOrBlank()
                            val isReviewed = markedForReview[q.id] == true

                            QuestionNumberPill(
                                number = index + 1,
                                isCurrent = isCurrent,
                                isAnswered = isAnswered,
                                isReviewed = isReviewed,
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Expandable Floater Button
                    Surface(
                        onClick = {
                            showPaletteSheet = true
                            onFloaterStateChanged(true)
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.GridView,
                                contentDescription = "Expand Questions Palette",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // ==================== 3. MIDDLE PORTION: QUESTIONS WITH OPTIONS ====================
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val q = questions[page]
                val isReviewed = markedForReview[q.id] == true

                val optionSeed = remember(studentSeed, q.id) {
                    (studentSeed + "_" + q.id).hashCode().toLong()
                }
                val shuffledOptions = remember(studentSeed, q.id, q.optionA, q.optionB, q.optionC, q.optionD) {
                    val rawOptions = listOf(
                        "a" to q.optionA,
                        "b" to q.optionB,
                        "c" to q.optionC,
                        "d" to q.optionD
                    ).shuffled(java.util.Random(optionSeed))
                    val badges = listOf("A", "B", "C", "D")
                    rawOptions.mapIndexed { idx, pair ->
                        ShuffledQuizOption(
                            originalKey = pair.first,
                            displayBadge = badges[idx],
                            text = pair.second
                        )
                    }
                }

                // Anti-Cheating: Block text copying / text selection
                DisableSelection {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Question Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    "QUESTION ${page + 1} OF ${questions.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            if (isReviewed) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Bookmark,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = Color(0xFF8B5CF6)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "FOR REVIEW",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF8B5CF6)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Question Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Text(
                                text = q.question,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(18.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Options List (Shuffled deterministically per student)
                        val selected = selectedAnswers[q.id] ?: ""
                        shuffledOptions.forEach { opt ->
                            val isSel = selected.equals(opt.originalKey, ignoreCase = true)
                            Card(
                                onClick = {
                                    if (isSel) {
                                        onAnswerSelected(q.id, "") // Tap to deselect
                                    } else {
                                        onAnswerSelected(q.id, opt.originalKey)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) BrandCyan.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    if (isSel) 2.dp else 1.dp,
                                    if (isSel) BrandCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(34.dp),
                                        shape = CircleShape,
                                        color = if (isSel) BrandCyan else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                opt.displayBadge,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSel) Color.Black else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Text(
                                        text = opt.text,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Clear Selection Button
                        if (selected.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { onAnswerSelected(q.id, "") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        Icons.Outlined.Clear,
                                        contentDescription = "Clear Selection",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Clear Selection",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            // ==================== 4. BOTTOM BAR ====================
            // Previous | Mark for Review | Next
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    OutlinedButton(
                        onClick = {
                            if (pagerState.currentPage > 0) {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            }
                        },
                        enabled = pagerState.currentPage > 0,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Previous", fontWeight = FontWeight.SemiBold)
                    }

                    // Mark for Review Button
                    val currentQuestion = questions[pagerState.currentPage]
                    val isCurrentReviewed = markedForReview[currentQuestion.id] == true

                    OutlinedButton(
                        onClick = { onToggleMarkForReview(currentQuestion.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isCurrentReviewed) Color(0xFF8B5CF6).copy(alpha = 0.15f) else Color.Transparent,
                            contentColor = if (isCurrentReviewed) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isCurrentReviewed) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (isCurrentReviewed) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isCurrentReviewed) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (isCurrentReviewed) "Marked" else "Review",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCurrentReviewed) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Next or Submit Button
                    if (pagerState.currentPage < questions.size - 1) {
                        Button(
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandCyan, contentColor = Color.Black),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onSubmit,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("Submit", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==================== IN-WINDOW EXPANDABLE FLOATER (NO SEPARATE WINDOW) ====================
        BackHandler(enabled = showPaletteSheet) {
            showPaletteSheet = false
            scope.launch {
                delay(200)
                onFloaterStateChanged(false)
            }
        }

        AnimatedVisibility(
            visible = showPaletteSheet,
            enter = fadeIn(animationSpec = tween(150)) + slideInVertically(initialOffsetY = { it }, animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showPaletteSheet = false
                        scope.launch {
                            delay(200)
                            onFloaterStateChanged(false)
                        }
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Absorb clicks inside surface so background tap isn't triggered */ },
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        // Grab Handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        )
                        Spacer(Modifier.height(12.dp))

                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Question Palette",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(onClick = {
                                showPaletteSheet = false
                                scope.launch {
                                    delay(200)
                                    onFloaterStateChanged(false)
                                }
                            }) {
                                Icon(Icons.Outlined.Close, contentDescription = "Close")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Summary Counters
                        val total = questions.size
                        val answeredCount = selectedAnswers.count { it.value.isNotEmpty() }
                        val reviewCount = markedForReview.count { it.value }
                        val unansweredCount = maxOf(0, total - answeredCount)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaletteStatusChip(
                                label = "Answered",
                                count = answeredCount,
                                color = Color(0xFF10B981),
                                modifier = Modifier.weight(1f)
                            )
                            PaletteStatusChip(
                                label = "Review",
                                count = reviewCount,
                                color = Color(0xFF8B5CF6),
                                modifier = Modifier.weight(1f)
                            )
                            PaletteStatusChip(
                                label = "Pending",
                                count = unansweredCount,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "Tap any question number to jump directly:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(12.dp))

                        // Questions Grid
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 52.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        ) {
                            itemsIndexed(questions) { index, q ->
                                val isCurrent = pagerState.currentPage == index
                                val isAnswered = !selectedAnswers[q.id].isNullOrBlank()
                                val isReviewed = markedForReview[q.id] == true

                                QuestionNumberPill(
                                    number = index + 1,
                                    isCurrent = isCurrent,
                                    isAnswered = isAnswered,
                                    isReviewed = isReviewed,
                                    size = 48.dp,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                            showPaletteSheet = false
                                            delay(200)
                                            onFloaterStateChanged(false)
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Legend
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LegendItem(color = BrandCyan, label = "Current")
                                LegendItem(color = Color(0xFF10B981), label = "Answered")
                                LegendItem(color = Color(0xFF8B5CF6), label = "Review")
                                LegendItem(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), label = "Pending")
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionNumberPill(
    number: Int,
    isCurrent: Boolean,
    isAnswered: Boolean,
    isReviewed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp
) {
    val reviewColor = Color(0xFF8B5CF6)
    val answeredColor = Color(0xFF10B981)
    val currentColor = BrandCyan

    val (bgColor, textColor, borderColor) = when {
        isReviewed && isAnswered -> Triple(reviewColor.copy(alpha = 0.25f), reviewColor, reviewColor)
        isReviewed -> Triple(reviewColor.copy(alpha = 0.2f), reviewColor, reviewColor.copy(alpha = 0.7f))
        isAnswered -> Triple(answeredColor.copy(alpha = 0.2f), answeredColor, answeredColor.copy(alpha = 0.7f))
        else -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrent) currentColor.copy(alpha = 0.22f) else bgColor)
            .border(
                width = if (isCurrent) 2.dp else 1.dp,
                color = if (isCurrent) currentColor else borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$number",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold
            ),
            color = if (isCurrent) currentColor else textColor
        )

        if (isReviewed) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(reviewColor)
            )
        }
    }
}

@Composable
private fun PaletteStatusChip(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
