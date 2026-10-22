package com.bagadbille.tdc.ui.mentor.quiz

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.model.QuizSubmissionDetail
import com.bagadbille.tdc.ui.components.ErrorScreen
import com.bagadbille.tdc.ui.components.LoadingScreen
import com.bagadbille.tdc.util.CsvExporter
import java.util.Locale

sealed class DynamicResultFilter {
    data object All : DynamicResultFilter()
    data class Top(val count: Int) : DynamicResultFilter()
    data object Disqualified : DynamicResultFilter()

    val label: String
        get() = when (this) {
            is All -> "All"
            is Top -> "Top $count"
            is Disqualified -> "Disqualified"
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultsScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf<DynamicResultFilter>(DynamicResultFilter.All) }
    var showCustomCutoffDialog by remember { mutableStateOf(false) }
    var customCutoffInput by remember { mutableStateOf("50") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Results", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is QuizResultsUiState.Success) {
                        val data = (uiState as QuizResultsUiState.Success).data
                        val validSubmissions = data.submissions.filter { it.submission.score >= 0 }
                        val disqualifiedSubmissions = data.submissions.filter { it.submission.score < 0 }
                        val displayedList = when (selectedFilter) {
                            is DynamicResultFilter.All -> data.submissions
                            is DynamicResultFilter.Top -> validSubmissions.take((selectedFilter as DynamicResultFilter.Top).count)
                            is DynamicResultFilter.Disqualified -> disqualifiedSubmissions
                        }

                        // Export CSV Action
                        IconButton(onClick = {
                            CsvExporter.exportQuizResults(
                                context = context,
                                quizTitle = data.quiz.title,
                                filterLabel = selectedFilter.label,
                                submissions = displayedList,
                                totalQuestions = data.stats.totalQuestions
                            )
                        }) {
                            Icon(
                                Icons.Outlined.FileDownload,
                                contentDescription = "Export CSV",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Copy Shortlist Action
                        IconButton(onClick = {
                            if (displayedList.isEmpty()) {
                                Toast.makeText(context, "No candidates in this filter", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            val text = displayedList.mapIndexed { i, s ->
                                val scoreText = if (s.submission.score >= 0) "${s.submission.score}/${data.stats.totalQuestions}" else "Disqualified"
                                "${i + 1}. ${s.studentName} (${s.studentEmail}) - Score: $scoreText"
                            }.joinToString("\n")
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "Copied ${displayedList.size} candidate(s) to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy Shortlist", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh Results", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (showCustomCutoffDialog) {
            var textValue by remember { mutableStateOf(customCutoffInput) }
            AlertDialog(
                onDismissRequest = { showCustomCutoffDialog = false },
                title = { Text("Set Custom Cutoff", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Enter the number of top students to include in the cohort selection:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    textValue = input
                                }
                            },
                            label = { Text("Top Students Count") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val count = textValue.toIntOrNull()?.coerceAtLeast(1) ?: 50
                            customCutoffInput = count.toString()
                            selectedFilter = DynamicResultFilter.Top(count)
                            showCustomCutoffDialog = false
                        }
                    ) {
                        Text("Apply")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomCutoffDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        when (val s = uiState) {
            is QuizResultsUiState.Loading -> LoadingScreen("Loading results...")
            is QuizResultsUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.load() })
            is QuizResultsUiState.Success -> {
                val data = s.data
                val stats = data.stats
                val validSubmissions = data.submissions.filter { it.submission.score >= 0 }
                val disqualifiedSubmissions = data.submissions.filter { it.submission.score < 0 }

                val displayedList = when (selectedFilter) {
                    is DynamicResultFilter.All -> data.submissions
                    is DynamicResultFilter.Top -> validSubmissions.take((selectedFilter as DynamicResultFilter.Top).count)
                    is DynamicResultFilter.Disqualified -> disqualifiedSubmissions
                }

                com.bagadbille.tdc.ui.components.TdcPullToRefresh(
                    isRefreshing = data.isRefreshing,
                    onRefresh = { viewModel.load(isPullToRefresh = true) }
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                    // Quiz Info Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    data.quiz.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = MaterialTheme.shapes.extraSmall,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            data.quiz.audience.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        shape = MaterialTheme.shapes.extraSmall,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            "${stats.totalQuestions} questions",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Stat summary cards
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuizStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.People,
                                value = "${stats.totalAttempts}",
                                label = "Attempts",
                                color = MaterialTheme.colorScheme.primary
                            )

                            val avgPercent = if (stats.totalQuestions > 0) {
                                ((stats.averageScore / stats.totalQuestions) * 100).toInt()
                            } else 0

                            QuizStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Analytics,
                                value = "$avgPercent%",
                                label = "Class Avg",
                                color = MaterialTheme.colorScheme.secondary
                            )

                            QuizStatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.EmojiEvents,
                                value = "${stats.highestScore}/${stats.totalQuestions}",
                                label = "Top Score",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    // Dynamic Cutoff Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Cohort Selection", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    val cutoffText = when (selectedFilter) {
                                        is DynamicResultFilter.Top -> {
                                            val c = (selectedFilter as DynamicResultFilter.Top).count
                                            val score = if (validSubmissions.size >= c) validSubmissions[c - 1].submission.score else validSubmissions.lastOrNull()?.submission?.score ?: 0
                                            val pct = if (stats.totalQuestions > 0) String.format(Locale.US, "%.1f%%", (score.toDouble() / stats.totalQuestions) * 100) else "0%"
                                            "Top $c Cutoff: $score/${stats.totalQuestions} ($pct)"
                                        }
                                        is DynamicResultFilter.Disqualified -> {
                                            "Disqualified: ${disqualifiedSubmissions.size} Candidates"
                                        }
                                        is DynamicResultFilter.All -> {
                                            val top50Score = if (validSubmissions.size >= 50) validSubmissions[49].submission.score else validSubmissions.lastOrNull()?.submission?.score ?: 0
                                            val pct = if (stats.totalQuestions > 0) String.format(Locale.US, "%.1f%%", (top50Score.toDouble() / stats.totalQuestions) * 100) else "0%"
                                            "Top 50 Cutoff: $top50Score/${stats.totalQuestions} ($pct)"
                                        }
                                    }
                                    Text(cutoffText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        "${displayedList.size} of ${data.submissions.size} Candidates Shown",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        CsvExporter.exportQuizResults(
                                            context = context,
                                            quizTitle = data.quiz.title,
                                            filterLabel = selectedFilter.label,
                                            submissions = displayedList,
                                            totalQuestions = stats.totalQuestions
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Export CSV", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    // Filter Chips Row
                    item {
                        Column {
                            Text(
                                "Candidate Filter",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedFilter is DynamicResultFilter.All,
                                        onClick = { selectedFilter = DynamicResultFilter.All },
                                        label = { Text("All (${data.submissions.size})") }
                                    )
                                }
                                listOf(30, 50, 70, 100).forEach { count ->
                                    item {
                                        FilterChip(
                                            selected = selectedFilter is DynamicResultFilter.Top && (selectedFilter as DynamicResultFilter.Top).count == count,
                                            onClick = { selectedFilter = DynamicResultFilter.Top(count) },
                                            label = { Text("Top $count (${minOf(count, validSubmissions.size)})") },
                                            leadingIcon = {
                                                Icon(
                                                    if (count <= 50) Icons.Outlined.CheckCircle else Icons.Outlined.Stars,
                                                    null,
                                                    Modifier.size(16.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                                val isCustomSelected = selectedFilter is DynamicResultFilter.Top &&
                                        (selectedFilter as DynamicResultFilter.Top).count !in setOf(30, 50, 70, 100)
                                item {
                                    FilterChip(
                                        selected = isCustomSelected,
                                        onClick = { showCustomCutoffDialog = true },
                                        label = {
                                            Text(
                                                if (isCustomSelected) "Custom: Top ${(selectedFilter as DynamicResultFilter.Top).count}"
                                                else "Custom..."
                                            )
                                        },
                                        leadingIcon = { Icon(Icons.Outlined.Tune, null, Modifier.size(16.dp)) }
                                    )
                                }
                                if (disqualifiedSubmissions.isNotEmpty()) {
                                    item {
                                        FilterChip(
                                            selected = selectedFilter is DynamicResultFilter.Disqualified,
                                            onClick = { selectedFilter = DynamicResultFilter.Disqualified },
                                            label = { Text("Disqualified (${disqualifiedSubmissions.size})") },
                                            leadingIcon = { Icon(Icons.Outlined.Cancel, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Submissions header
                    item {
                        Text(
                            "Submissions (${displayedList.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Submissions list or empty state
                    if (displayedList.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Outlined.Inbox,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "No submissions found",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "No candidate submissions match this filter.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(displayedList, key = { _, detail -> detail.submission.studentUid }) { index, detail ->
                            // Determine true rank based on position in validSubmissions
                            val trueRank = if (detail.submission.score >= 0) {
                                validSubmissions.indexOfFirst { it.submission.studentUid == detail.submission.studentUid } + 1
                            } else null

                            val activeCutoff = (selectedFilter as? DynamicResultFilter.Top)?.count

                            StudentSubmissionCard(
                                detail = detail,
                                rank = trueRank,
                                totalQuestions = stats.totalQuestions,
                                activeCutoff = activeCutoff
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
private fun QuizStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StudentSubmissionCard(
    detail: QuizSubmissionDetail,
    rank: Int?,
    totalQuestions: Int,
    activeCutoff: Int? = null
) {
    val initials = detail.studentName
        .split(" ").filter { it.isNotBlank() }.take(2)
        .map { it.first().uppercase() }.joinToString("")
        .ifEmpty { "??" }

    val isDisqualified = detail.submission.score < 0

    val percentage = if (totalQuestions > 0 && !isDisqualified) {
        ((detail.submission.score.toDouble() / totalQuestions) * 100).toInt()
    } else 0

    val badgeColor = when {
        isDisqualified -> MaterialTheme.colorScheme.error
        percentage >= 80 -> MaterialTheme.colorScheme.primary
        percentage >= 50 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, if (isDisqualified) MaterialTheme.colorScheme.error.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank or Avatar
            if (rank != null) {
                val isHighlighted = if (activeCutoff != null) rank <= activeCutoff else rank <= 50
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "#$rank",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        detail.studentName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (rank != null && activeCutoff != null && rank <= activeCutoff) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Top $activeCutoff",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    } else if (rank != null && rank <= 50) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Top 50",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    } else if (rank != null && rank <= 70) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Top 70",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    detail.studentEnrollment?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                    if (detail.studentEmail.isNotBlank()) {
                        Text(
                            detail.studentEmail,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Timing details: Total time taken and submission time
                val enteringTimeStr = formatDisplayTime(detail.submission.startedAt)
                val exitTimeStr = formatDisplayTime(detail.submission.submittedAt)
                val durationStr = formatDurationDisplay(detail.submission.timeTakenSeconds, detail.submission.startedAt, detail.submission.submittedAt)

                if (enteringTimeStr != null || exitTimeStr != null || durationStr != null) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (durationStr != null) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    "⏱️ $durationStr",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (exitTimeStr != null) {
                            Text(
                                "Exit: $exitTimeStr",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                shape = MaterialTheme.shapes.small,
                color = badgeColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isDisqualified) {
                        Text(
                            "Disqualified",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "3 Strikes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            "${detail.submission.score} / $totalQuestions",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                        Text(
                            "$percentage%",
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor
                        )
                    }
                }
            }
        }
    }
}

private fun formatDisplayTime(isoString: String?): String? {
    if (isoString.isNullOrBlank()) return null
    return try {
        val parseFormats = listOf(
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") },
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") },
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US),
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        )
        val outFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        for (pf in parseFormats) {
            try {
                val d = pf.parse(isoString)
                if (d != null) return outFormat.format(d)
            } catch (_: Exception) {}
        }
        null
    } catch (_: Exception) {
        null
    }
}

private fun formatDurationDisplay(seconds: Int?, startedAt: String?, submittedAt: String?): String? {
    val totalSec = if (seconds != null && seconds > 0) {
        seconds
    } else {
        val startMs = parseIsoDateMillis(startedAt)
        val subMs = parseIsoDateMillis(submittedAt)
        if (startMs != null && subMs != null && subMs >= startMs) {
            ((subMs - startMs) / 1000).toInt()
        } else null
    }

    if (totalSec == null || totalSec <= 0) return null
    val m = totalSec / 60
    val s = totalSec % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}

private fun parseIsoDateMillis(isoString: String?): Long? {
    if (isoString.isNullOrBlank()) return null
    val parseFormats = listOf(
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") },
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") },
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", java.util.Locale.US),
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
    )
    for (pf in parseFormats) {
        try {
            val d = pf.parse(isoString)
            if (d != null) return d.time
        } catch (_: Exception) {}
    }
    return null
}
