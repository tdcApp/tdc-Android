package com.bagadbille.tdc.ui.home.classes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.model.Schedule
import com.bagadbille.tdc.ui.components.CyberCard
import com.bagadbille.tdc.ui.components.EmptyStateScreen
import com.bagadbille.tdc.ui.components.ErrorScreen
import com.bagadbille.tdc.ui.components.LoadingScreen
import com.bagadbille.tdc.ui.components.TdcPullToRefresh
import com.bagadbille.tdc.ui.theme.BrandCyan
import com.bagadbille.tdc.ui.theme.BrandOrange
import java.util.Calendar

private val dayFullNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

@Composable
fun ClassesScreen(viewModel: ClassesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is ClassesUiState.Loading -> LoadingScreen()
        is ClassesUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.loadClasses() })
        is ClassesUiState.Success -> {
            val data = s.data
            val today = remember { Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 }

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
                sortedList
            }

            TdcPullToRefresh(
                isRefreshing = false,
                onRefresh = { viewModel.loadClasses() }
            ) {
                if (upcomingClasses.isEmpty()) {
                    EmptyStateScreen(
                        icon = Icons.Outlined.School,
                        title = "No Upcoming Classes",
                        message = "There are no classes scheduled right now. Enjoy your break! 🎉"
                    )
                } else {
                    val classesMap = remember(data.enrolledClasses) { data.enrolledClasses.associateBy { it.id } }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                                Text(
                                    text = "Upcoming Classes",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Your scheduled batch sessions and live classes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(upcomingClasses, key = { "${it.first.id}_${it.second}" }) { (schedule, dayLabel) ->
                            val batchName = classesMap[schedule.classId]?.name
                            UpcomingClassItem(schedule = schedule, dayLabel = dayLabel, batchName = batchName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingClassItem(
    schedule: Schedule,
    dayLabel: String,
    batchName: String? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == com.bagadbille.tdc.ui.theme.DarkBackground
    val isToday = dayLabel == "Today"
    val isTomorrow = dayLabel == "Tomorrow"
    val badgeColor = when {
        isToday -> if (isDark) BrandCyan else MaterialTheme.colorScheme.primary
        isTomorrow -> if (isDark) BrandOrange else MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        borderColor = if (isToday) badgeColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day relative badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.16f))
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeColor
                    )
                }

                // Batch tag if available
                if (!batchName.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = batchName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Subject name
            Text(
                text = schedule.subject,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(10.dp))

            // Time & Location row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = formatTimeRange(schedule.startTime, schedule.endTime),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!schedule.room.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = schedule.room,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimeRange(startTime: String, endTime: String): String {
    return try {
        fun formatSingleTime(t: String): String {
            val parts = t.split(":")
            val h = parts[0].toInt()
            val m = parts.getOrNull(1)?.toInt() ?: 0
            val amPm = if (h >= 12) "PM" else "AM"
            val h12 = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            return if (m == 0) "$h12 $amPm" else String.format("%d:%02d %s", h12, m, amPm)
        }
        "${formatSingleTime(startTime)} - ${formatSingleTime(endTime)}"
    } catch (_: Exception) {
        "$startTime - $endTime"
    }
}
