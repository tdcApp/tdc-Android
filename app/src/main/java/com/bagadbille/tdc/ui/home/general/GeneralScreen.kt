package com.bagadbille.tdc.ui.home.general

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.model.Quiz
import com.bagadbille.tdc.data.model.Schedule
import com.bagadbille.tdc.ui.components.CyberCard
import com.bagadbille.tdc.ui.components.ErrorScreen
import com.bagadbille.tdc.ui.components.LoadingScreen
import com.bagadbille.tdc.ui.theme.*
import com.bagadbille.tdc.util.MeetingHelper
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.ui.graphics.Brush
import com.bagadbille.tdc.ui.components.TdcButton
import java.util.Calendar

private val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
private val dayFullNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

@Composable
fun GeneralScreen(
    onQuizClick: (String) -> Unit = {},
    onQuizResultClick: (String) -> Unit = {},
    onNavigateToClasses: () -> Unit = {},
    onNavigateToQuizzes: () -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
    onNavigateToNotices: () -> Unit = {},
    viewModel: GeneralViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val s = uiState) {
        is GeneralUiState.Loading -> LoadingScreen()
        is GeneralUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.loadDashboard() })
        is GeneralUiState.Success -> com.bagadbille.tdc.ui.components.TdcPullToRefresh(
            isRefreshing = false,
            onRefresh = { viewModel.loadDashboard() }
        ) {
            DashboardContent(
                data = s.data,
                onQuizClick = onQuizClick,
                onQuizResultClick = onQuizResultClick,
                onNavigateToClasses = onNavigateToClasses,
                onNavigateToQuizzes = onNavigateToQuizzes,
                onNavigateToAssignments = onNavigateToAssignments,
                onNavigateToNotices = onNavigateToNotices
            )
        }
    }
}

@Composable
private fun DashboardContent(
    data: DashboardData,
    onQuizClick: (String) -> Unit,
    onQuizResultClick: (String) -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToQuizzes: () -> Unit,
    onNavigateToAssignments: () -> Unit,
    onNavigateToNotices: () -> Unit
) {
    val context = LocalContext.current
    val today = remember { Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 } // 0=Sun, 1=Mon, ..., 6=Sat

    // Compute upcoming classes starting from today onwards
    val upcomingClasses = remember(data.schedules, today) {
        val orderedDays = (today..6) + (0 until today)
        val sortedList = mutableListOf<Pair<Schedule, String>>()
        for (day in orderedDays) {
            val dayLabel = when (day) {
                today -> "Today"
                (today + 1) % 7 -> "Tomorrow"
                else -> dayFullNames[day]
            }
            val dayItems = data.schedules.filter { it.dayOfWeek == day }.sortedBy { it.startTime }
            dayItems.forEach { sortedList.add(it to dayLabel) }
        }
        sortedList.take(4)
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ─── 1. USER GREETING ───
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Text(
                    text = if (data.userName.isNotBlank()) "Hey ${data.userName} 👋" else "Hey Developer 👋",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Build something amazing today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ─── 2. TDC CODING QUEST HERO BANNER ───
        item {
            val isDark = MaterialTheme.colorScheme.background == DarkBackground
            val specialQuiz = data.specialQuiz
            val isComingSoon = specialQuiz == null || 
                               specialQuiz.title.contains("Coming Soon", ignoreCase = true) ||
                               specialQuiz.questionCount == 0
            val isCompleted = specialQuiz?.isSubmitted == true
            val isQuizReady = specialQuiz != null && !isComingSoon && !isCompleted
            val formUrl = "https://forms.gle/nkSmmE9EU31os5UU8"

            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
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
                        BrandCyan.copy(alpha = if (isDark) 0.8f else 0.5f),
                        BrandOrange.copy(alpha = if (isDark) 0.8f else 0.5f)
                    )
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isCompleted -> BrandCyan.copy(alpha = 0.2f)
                                        specialQuiz?.hasActiveAttempt == true -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                                        isQuizReady -> BrandOrange.copy(alpha = 0.25f)
                                        else -> if (isDark) BrandOrange.copy(alpha = 0.2f) else Color(0xFFFFEDD5)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isCompleted -> BrandCyan.copy(alpha = 0.5f)
                                        specialQuiz?.hasActiveAttempt == true -> MaterialTheme.colorScheme.tertiary
                                        isQuizReady -> BrandOrange
                                        else -> if (isDark) BrandOrange.copy(alpha = 0.4f) else Color(0xFFFDBA74)
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when {
                                    isCompleted -> "QUEST COMPLETED"
                                    specialQuiz?.hasActiveAttempt == true -> "⚡ IN PROGRESS"
                                    isQuizReady -> "⚡ LIVE CHALLENGE"
                                    else -> "COMING SOON"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = when {
                                    isCompleted -> BrandCyan
                                    specialQuiz?.hasActiveAttempt == true -> MaterialTheme.colorScheme.tertiary
                                    isQuizReady -> if (isDark) BrandOrange else Color(0xFFC2410C)
                                    else -> if (isDark) BrandOrange else Color(0xFFC2410C)
                                }
                            )
                        }

                        if (specialQuiz != null && specialQuiz.questionCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BrandCyan.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, BrandCyan.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "${specialQuiz.questionCount} Questions",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BrandCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = specialQuiz?.title?.ifBlank { null } ?: "TDC Coding Quest Season 3",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when {
                            isCompleted -> "Your responses have been securely submitted • Evaluation in progress"
                            isQuizReady -> "The official contest is live! Tap below to start your proctored challenge."
                            else -> "Innovate • Build • Collaborate"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF475569)
                    )

                    Spacer(Modifier.height(18.dp))

                    when {
                        isCompleted -> {
                            TdcButton(
                                text = "View Result",
                                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                                onClick = { onQuizResultClick(specialQuiz.id) },
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                        isQuizReady -> {
                            TdcButton(
                                text = if (specialQuiz.hasActiveAttempt) "Resume Quiz" else "Join Now",
                                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                                onClick = { onQuizClick(specialQuiz.id) },
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                        else -> {
                            TdcButton(
                                text = "Register Now",
                                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.wrapContentWidth()
                            )
                        }
                    }
                }
            }
        }

        // ─── 3. QUICK ACCESS GRID ───
        item {
            Column {
                Text(
                    text = "Quick Access",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickAccessCard(
                        icon = Icons.Outlined.Class,
                        label = "Classes",
                        accentColor = BrandCyan,
                        onClick = onNavigateToClasses,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessCard(
                        icon = Icons.Outlined.Quiz,
                        label = "Quizzes",
                        accentColor = BrandOrange,
                        onClick = onNavigateToQuizzes,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessCard(
                        icon = Icons.AutoMirrored.Outlined.Assignment,
                        label = "Assignments",
                        accentColor = BrandCyan,
                        onClick = onNavigateToAssignments,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessCard(
                        icon = Icons.Outlined.Campaign,
                        label = "Notices",
                        accentColor = BrandCoral,
                        onClick = onNavigateToNotices,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ─── 4. RECENT UPDATES / ANNOUNCEMENTS ───
        if (data.recentAnnouncements.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Updates",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToNotices() }
                    )
                }
            }
            items(data.recentAnnouncements, key = { it.id }) { announcement ->
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToNotices() },
                    shape = RoundedCornerShape(16.dp),
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandCyan.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Campaign,
                                contentDescription = null,
                                tint = BrandCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = announcement.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = announcement.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // ─── 5. WEEKLY SCHEDULE WIDGET ───
        item {
            WeeklyScheduleWidget(
                schedules = data.schedules,
                today = today,
                onNavigateToBatches = onNavigateToClasses
            )
        }

        // ─── 6. COMBINED UPCOMING (1 CLASS, 1 QUIZ) ───
        val nextClass = upcomingClasses.firstOrNull()
        val nextQuiz = data.upcomingQuizzes.firstOrNull()

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "View Classes →",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToClasses() }
                )
            }
        }

        if (nextClass == null && nextQuiz == null) {
            item {
                EmptyCard("No upcoming classes or quizzes scheduled. You're all caught up! 🎉")
            }
        } else {
            // Next 1 upcoming class
            nextClass?.let { (schedule, dayRelative) ->
                item {
                    UpcomingClassCard(
                        schedule = schedule,
                        dayLabel = dayRelative,
                        onClick = onNavigateToClasses
                    )
                }
            }

            // Next 1 upcoming quiz
            nextQuiz?.let { quiz ->
                item {
                    UpcomingQuizCard(
                        quiz = quiz,
                        onClick = { onQuizClick(quiz.id) }
                    )
                }
            }
        }

        // Bottom spacing
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun QuickAccessCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == DarkBackground
    val resolvedColor = if (isDark) {
        accentColor
    } else {
        when (accentColor) {
            BrandCyan -> MaterialTheme.colorScheme.primary
            BrandOrange -> MaterialTheme.colorScheme.secondary
            else -> accentColor
        }
    }

    CyberCard(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        borderColor = MaterialTheme.colorScheme.outlineVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(resolvedColor.copy(alpha = if (isDark) 0.12f else 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = resolvedColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// WEEKLY SCHEDULE WIDGET — Interactive 7-Day Week View
// ═══════════════════════════════════════════════════════

@Composable
private fun WeeklyScheduleWidget(
    schedules: List<Schedule>,
    today: Int,
    onNavigateToBatches: () -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(today) }
    val grouped = remember(schedules) { schedules.groupBy { it.dayOfWeek } }
    val selectedDaySchedules = remember(grouped, selectedDay) {
        (grouped[selectedDay] ?: emptyList()).sortedBy { it.startTime }
    }

    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        borderColor = GlassBorderSubtle
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header Row: Schedule Icon + Title + Batches shortcut
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToBatches() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonEmerald.copy(alpha = 0.12f))
                            .border(1.dp, NeonEmerald.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = NeonEmerald
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Weekly Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Selected Day Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonCyan.copy(alpha = 0.12f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (selectedDay == today) "Today" else dayFullNames[selectedDay],
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan
                        )
                    }

                    Text(
                        "Classes →",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // 7-Day Rectangular Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                dayLabels.forEachIndexed { dayIndex, label ->
                    val daySchedules = grouped[dayIndex] ?: emptyList()
                    val isToday = dayIndex == today
                    val isSelected = dayIndex == selectedDay

                    DayCell(
                        label = label,
                        classCount = daySchedules.size,
                        isToday = isToday,
                        isSelected = isSelected,
                        onClick = { selectedDay = dayIndex },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            // Classes for Selected Day
            AnimatedContent(
                targetState = selectedDaySchedules,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "schedule_transition"
            ) { daySchedules ->
                if (daySchedules.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "No classes on ${dayFullNames[selectedDay]}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        daySchedules.forEach { schedule ->
                            ScheduleRowItem(schedule)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    label: String,
    classCount: Int,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == com.bagadbille.tdc.ui.theme.DarkBackground

    val bgColor = when {
        isSelected -> if (isDark) NeonEmerald else MaterialTheme.colorScheme.primary
        isToday -> if (isDark) NeonEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        classCount > 0 -> if (isDark) DarkSurfaceVariant else MaterialTheme.colorScheme.surfaceVariant
        else -> if (isDark) DarkSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val textColor = when {
        isSelected -> if (isDark) Color(0xFF070A12) else Color.White
        isToday -> if (isDark) NeonEmerald else MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderColor = when {
        isSelected -> if (isDark) NeonEmerald else MaterialTheme.colorScheme.primary
        isToday -> if (isDark) NeonEmerald.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        else -> if (isDark) GlassBorderSubtle else MaterialTheme.colorScheme.outlineVariant
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day Label
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )

        Spacer(Modifier.height(4.dp))

        // Class Count / Status
        if (classCount > 0) {
            Text(
                "$classCount",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        } else {
            Text(
                "–",
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.35f)
            )
        }

        Spacer(Modifier.height(4.dp))

        // Small indicator dot for "Today"
        if (isToday) {
            Box(
                Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF070A12) else NeonEmerald)
            )
        } else {
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun LiveNowBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_pulse"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NeonEmerald.copy(alpha = 0.15f))
            .border(1.dp, NeonEmerald.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(NeonEmerald.copy(alpha = alpha))
            )
            Text(
                "LIVE NOW",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.ExtraBold,
                color = NeonEmerald,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun ScheduleRowItem(schedule: Schedule) {
    val context = LocalContext.current
    val isLive = remember(schedule) { MeetingHelper.isScheduleLive(schedule) }
    val isOnline = remember(schedule.room) { MeetingHelper.isOnlineMeeting(schedule.room) }

    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        borderColor = if (isLive) NeonEmerald.copy(alpha = 0.5f) else GlassBorderSubtle
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isLive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, if (isLive) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "${schedule.startTime} - ${schedule.endTime}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isLive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.width(10.dp))

            // Subject & Platform
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        schedule.subject,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isLive) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                schedule.room?.let { platform ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "📍 $platform",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isOnline) {
                Spacer(Modifier.width(8.dp))
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberPrimaryGradient)
                            .clickable { MeetingHelper.launchMeeting(context, schedule.room) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Videocam, null, Modifier.size(14.dp), tint = Color(0xFF070A12))
                            Spacer(Modifier.width(4.dp))
                            Text("Join", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF070A12))
                        }
                    }
                } else {
                    IconButton(
                        onClick = { MeetingHelper.launchMeeting(context, schedule.room) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Videocam,
                            contentDescription = "Join Class",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// SECTION HEADERS & CARDS
// ═══════════════════════════════════════════════════════

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    count: Int,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeonEmerald.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(16.dp), tint = NeonEmerald)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (count > 0) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonEmerald.copy(alpha = 0.15f))
                        .border(1.dp, NeonEmerald.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "$count",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonEmerald
                    )
                }
            }
        }

        if (actionLabel != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun UpcomingClassCard(
    schedule: Schedule,
    dayLabel: String,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isLive = remember(schedule) { MeetingHelper.isScheduleLive(schedule) }
    val isOnline = remember(schedule.room) { MeetingHelper.isOnlineMeeting(schedule.room) }

    CyberCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        borderColor = if (isLive) NeonEmerald.copy(alpha = 0.45f) else GlassBorderSubtle
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Relative Day + Time badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isLive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (isLive) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isLive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            schedule.startTime,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            schedule.subject,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isLive) {
                            Spacer(Modifier.width(6.dp))
                            LiveNowBadge()
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${schedule.startTime} - ${schedule.endTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        schedule.room?.let { platform ->
                            Text(
                                " • 📍 $platform",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Quick Join Action Button
            if (isOnline) {
                Spacer(Modifier.height(12.dp))
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberPrimaryGradient)
                            .clickable { MeetingHelper.launchMeeting(context, schedule.room) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Videocam, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF070A12))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Join Live Class Now",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF070A12)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .clickable { MeetingHelper.launchMeeting(context, schedule.room) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Videocam, contentDescription = null, modifier = Modifier.size(16.dp), tint = NeonCyan)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Join Meeting / Classroom Link",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingQuizCard(quiz: Quiz, onClick: () -> Unit) {
    CyberCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        borderColor = GlassBorderSubtle
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonPurple.copy(alpha = 0.12f))
                    .border(1.dp, NeonPurple.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Quiz,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = NeonPurple
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    quiz.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            quiz.audience.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    quiz.createdAt?.let {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Outlined.AccessTime,
                            null,
                            Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            it.take(10),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "Take Quiz",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyCard(message: String) {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            message,
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
