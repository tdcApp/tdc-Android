package com.bagadbille.tdc.ui.assignments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.ui.components.CyberCard
import com.bagadbille.tdc.ui.components.EmptyStateScreen
import com.bagadbille.tdc.ui.components.ErrorScreen
import com.bagadbille.tdc.ui.components.LoadingScreen
import com.bagadbille.tdc.ui.theme.*
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch

@Composable
fun AssignmentsScreen(
    onNavigateToAssignmentDetail: (String) -> Unit,
    onNotificationClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = false,
    viewModel: AssignmentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("Ongoing", "Past")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // ─── Header: Matching HomeScreen (Logo | TDC | Notification Bell) ───
        com.bagadbille.tdc.ui.components.TdcHeader(
            onNotificationClick = onNotificationClick,
            hasUnreadNotifications = hasUnreadNotifications
        )

        // ─── TabRow: Matching HomeScreen (transparent container, primary active indicator, theme divider) ───
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            },
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.then(with(TabRowDefaults) { Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]) }),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(
                            title,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ─── Content Pages ───
        when (val s = uiState) {
            is AssignmentsUiState.Loading -> LoadingScreen("Loading assignments...")
            is AssignmentsUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.loadAssignments() })
            is AssignmentsUiState.Success -> {
                com.bagadbille.tdc.ui.components.TdcPullToRefresh(
                    isRefreshing = false,
                    onRefresh = { viewModel.loadAssignments() }
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                    val assignments = if (page == 0) s.ongoingAssignments else s.pastAssignments
                    val sectionTitle = if (page == 0) "Active Assignments" else "Past Assignments"

                    if (assignments.isEmpty()) {
                        EmptyStateScreen(
                            Icons.AutoMirrored.Outlined.Assignment,
                            if (page == 0) "No Ongoing Assignments" else "No Past Assignments",
                            if (page == 0) "You're all caught up! Great job 🎉" else "No past assignments recorded."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Section Header matching Home Screen style
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        sectionTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NeonEmerald.copy(alpha = 0.15f))
                                            .border(1.dp, NeonEmerald.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "${assignments.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonEmerald
                                        )
                                    }
                                }
                            }

                            items(assignments, key = { it.assignment.id }) { item ->
                                AssignmentCard(
                                    item = item,
                                    onClick = { onNavigateToAssignmentDetail(item.assignment.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

// ─── Card Design: Perfectly matching ClassCard & QuizCard on HomeScreen ───
@Composable
private fun AssignmentCard(
    item: AssignmentItem,
    onClick: () -> Unit
) {
    val isSubmitted = item.submission != null

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val borderColor = when {
        item.isOverdue -> ErrorRed.copy(alpha = 0.4f)
        isSubmitted -> primaryColor.copy(alpha = 0.35f)
        else -> GlassBorderSubtle
    }

    val iconTint = when {
        isSubmitted -> primaryColor
        item.isOverdue -> ErrorRed
        else -> secondaryColor
    }

    CyberCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        borderColor = borderColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.12f))
                    .border(1.dp, iconTint.copy(alpha = 0.28f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when {
                        isSubmitted -> Icons.Outlined.CheckCircle
                        item.isOverdue -> Icons.Outlined.Warning
                        else -> Icons.Outlined.Code
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )
            }

            Spacer(Modifier.width(14.dp))

            // Middle Column (Title, Class/Audience Tag, Due Date)
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.assignment.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(5.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Class badge — constrained so it doesn't push date off-screen
                    item.className?.let { className ->
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                className,
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Due date — non-shrinkable
                    item.assignment.dueAt?.let { due ->
                        if (item.className != null) Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Outlined.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            due.take(10),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Creator name: ADMIN or Teacher name
                val creator = item.assignment.createdByName?.ifBlank { null }
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

            // Right Action / Status Pill
            when {
                isSubmitted -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(primaryColor.copy(alpha = 0.15f))
                            .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = primaryColor
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Done",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }
                }
                item.isOverdue -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ErrorRed.copy(alpha = 0.15f))
                            .border(1.dp, ErrorRed.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Overdue",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor.copy(alpha = 0.15f))
                            .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Submit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = primaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}
