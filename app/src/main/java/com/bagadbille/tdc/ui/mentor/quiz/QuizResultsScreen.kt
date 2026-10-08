package com.bagadbille.tdc.ui.mentor.quiz

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.model.QuizSubmissionDetail
import com.bagadbille.tdc.ui.components.ErrorScreen
import com.bagadbille.tdc.ui.components.LoadingScreen

enum class ResultFilter(val label: String) {
    ALL("All"),
    TOP_50("Top 50"),
    TOP_70("Top 70"),
    DISQUALIFIED("Disqualified")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultsScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf(ResultFilter.ALL) }
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
                        val candidatesToCopy = when (selectedFilter) {
                            ResultFilter.TOP_70 -> validSubmissions.take(70)
                            ResultFilter.DISQUALIFIED -> data.submissions.filter { it.submission.score < 0 }
                            else -> validSubmissions.take(50)
                        }
                        IconButton(onClick = {
                            if (candidatesToCopy.isEmpty()) {
                                Toast.makeText(context, "No candidates in this filter", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            val text = candidatesToCopy.mapIndexed { i, s ->
                                val scoreText = if (s.submission.score >= 0) "${s.submission.score}/${data.stats.totalQuestions}" else "Disqualified"
                                "${i + 1}. ${s.studentName} (${s.studentEmail}) - Score: $scoreText"
                            }.joinToString("\n")
                            clipboardManager.setText(AnnotatedString(text))
                            Toast.makeText(context, "Copied ${candidatesToCopy.size} candidate(s) to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy Shortlist", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        when (val s = uiState) {
            is QuizResultsUiState.Loading -> LoadingScreen("Loading results...")
            is QuizResultsUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.load() })
            is QuizResultsUiState.Success -> {
                val data = s.data
                val stats = data.stats
                val validSubmissions = data.submissions.filter { it.submission.score >= 0 }
                val disqualifiedSubmissions = data.submissions.filter { it.submission.score < 0 }

                val displayedList = when (selectedFilter) {
                    ResultFilter.ALL -> data.submissions
                    ResultFilter.TOP_50 -> validSubmissions.take(50)
                    ResultFilter.TOP_70 -> validSubmissions.take(70)
                    ResultFilter.DISQUALIFIED -> disqualifiedSubmissions
                }

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

                    // Top 50 / 70 Cutoff Banner
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
                                Column {
                                    Text("Cohort Selection", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    val top50Cutoff = if (validSubmissions.size >= 50) "${validSubmissions[49].submission.score}/${stats.totalQuestions}" else "${validSubmissions.lastOrNull()?.submission?.score ?: 0}/${stats.totalQuestions}"
                                    Text("Top 50 Cutoff: $top50Cutoff", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "${validSubmissions.size} Candidates Qualified",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
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
                                        selected = selectedFilter == ResultFilter.ALL,
                                        onClick = { selectedFilter = ResultFilter.ALL },
                                        label = { Text("All (${data.submissions.size})") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedFilter == ResultFilter.TOP_50,
                                        onClick = { selectedFilter = ResultFilter.TOP_50 },
                                        label = { Text("Top 50 (${minOf(50, validSubmissions.size)})") },
                                        leadingIcon = { Icon(Icons.Outlined.CheckCircle, null, Modifier.size(16.dp)) }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = selectedFilter == ResultFilter.TOP_70,
                                        onClick = { selectedFilter = ResultFilter.TOP_70 },
                                        label = { Text("Top 70 (${minOf(70, validSubmissions.size)})") },
                                        leadingIcon = { Icon(Icons.Outlined.Stars, null, Modifier.size(16.dp)) }
                                    )
                                }
                                if (disqualifiedSubmissions.isNotEmpty()) {
                                    item {
                                        FilterChip(
                                            selected = selectedFilter == ResultFilter.DISQUALIFIED,
                                            onClick = { selectedFilter = ResultFilter.DISQUALIFIED },
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

                            StudentSubmissionCard(
                                detail = detail,
                                rank = trueRank,
                                totalQuestions = stats.totalQuestions
                            )
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
    totalQuestions: Int
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
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = if (rank <= 50) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "#$rank",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (rank <= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                    if (rank != null && rank <= 50) {
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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    detail.studentEnrollment?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "· ${detail.studentEmail}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
