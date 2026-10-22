package com.bagadbille.tdc.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.model.Announcement
import com.bagadbille.tdc.data.model.AppNotification
import com.bagadbille.tdc.data.model.NotificationType
import com.bagadbille.tdc.ui.components.EmptyStateScreen
import com.bagadbille.tdc.ui.components.ErrorScreen
import com.bagadbille.tdc.ui.components.LoadingScreen
import kotlinx.coroutines.launch

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.bagadbille.tdc.ui.components.CyberCard
import com.bagadbille.tdc.ui.components.TdcPullToRefresh
import com.bagadbille.tdc.ui.theme.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToQuiz: (String) -> Unit = {},
    onNavigateToAssignment: (String) -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Automatically load fresh notifications every time screen is opened
    LaunchedEffect(Unit) {
        viewModel.loadAll(isPullToRefresh = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAll(isPullToRefresh = true) }) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(
                            Icons.Outlined.DoneAll,
                            contentDescription = "Mark all as read",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding).fillMaxSize()) {
            when (val s = uiState) {
                is NotificationsUiState.Loading -> LoadingScreen()
                is NotificationsUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.loadAll() })
                is NotificationsUiState.Success -> TdcPullToRefresh(
                    isRefreshing = s.data.isRefreshing,
                    onRefresh = { viewModel.loadAll(isPullToRefresh = true) }
                ) {
                    NotificationsContent(
                        data = s.data,
                        onMarkAsRead = { viewModel.markAsRead(it) },
                        onNavigateToQuiz = onNavigateToQuiz,
                        onNavigateToAssignment = onNavigateToAssignment,
                        onNavigateToSchedule = onNavigateToSchedule
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsContent(
    data: NotificationsData,
    onMarkAsRead: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateToAssignment: (String) -> Unit,
    onNavigateToSchedule: () -> Unit
) {
    val tabs = listOf("Notifications", "Announcements")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    var selectedNotif by remember { mutableStateOf<AppNotification?>(null) }
    var selectedAnnouncement by remember { mutableStateOf<Announcement?>(null) }

    Column(Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title, style = MaterialTheme.typography.labelLarge) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                0 -> NotificationsTab(
                    notifications = data.notifications,
                    onNotificationClick = { n ->
                        selectedNotif = n
                        onMarkAsRead(n.id)
                    }
                )
                1 -> AnnouncementsTab(
                    announcements = data.announcements,
                    onAnnouncementClick = { a ->
                        selectedAnnouncement = a
                        onMarkAsRead("ann_${a.id}")
                    }
                )
            }
        }
    }

    selectedNotif?.let { notif ->
        AlertDialog(
            onDismissRequest = { selectedNotif = null },
            icon = { Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(notif.title) },
            text = {
                Column {
                    Text(notif.body, style = MaterialTheme.typography.bodyMedium)
                    if (notif.createdAt.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            notif.createdAt.take(16).replace('T', ' '),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedNotif = null }) { Text("Close") }
            }
        )
    }

    selectedAnnouncement?.let { ann ->
        val tagStyle = getAnnouncementTagStyle(ann.tag, ann.targetScreen)
        val targetScreenUpper = (ann.targetScreen ?: ann.tag).uppercase()
        val hasTargetAction = targetScreenUpper in listOf("QUIZ", "ASSIGNMENT", "SCHEDULE")

        AlertDialog(
            onDismissRequest = { selectedAnnouncement = null },
            icon = { Icon(tagStyle.icon, contentDescription = null, tint = tagStyle.color) },
            title = { Text(ann.title) },
            text = {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = tagStyle.color.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, tagStyle.color.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(tagStyle.icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = tagStyle.color)
                                Text(
                                    tagStyle.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = tagStyle.color
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Text(
                                "Audience: ${ann.audience.replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(ann.content, style = MaterialTheme.typography.bodyMedium)
                    ann.createdAt?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            it.take(16).replace('T', ' '),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            confirmButton = {
                if (hasTargetAction) {
                    Button(
                        onClick = {
                            val tId = ann.targetId ?: ""
                            selectedAnnouncement = null
                            when (targetScreenUpper) {
                                "QUIZ" -> onNavigateToQuiz(tId)
                                "ASSIGNMENT" -> onNavigateToAssignment(tId)
                                "SCHEDULE" -> onNavigateToSchedule()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = tagStyle.color)
                    ) {
                        Text(
                            when (targetScreenUpper) {
                                "QUIZ" -> if (!ann.targetId.isNullOrBlank()) "Take Quiz" else "Open Quizzes"
                                "ASSIGNMENT" -> if (!ann.targetId.isNullOrBlank()) "Open Assignment" else "View Assignments"
                                "SCHEDULE" -> "View Schedule"
                                else -> "Open"
                            },
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    TextButton(onClick = { selectedAnnouncement = null }) { Text("Close") }
                }
            },
            dismissButton = if (hasTargetAction) {
                { TextButton(onClick = { selectedAnnouncement = null }) { Text("Close") } }
            } else null
        )
    }
}

// ─── Notifications Tab ───

@Composable
private fun NotificationsTab(
    notifications: List<AppNotification>,
    onNotificationClick: (AppNotification) -> Unit
) {
    if (notifications.isEmpty()) {
        EmptyStateScreen(Icons.Outlined.NotificationsNone, "No Notifications", "You're all caught up!")
    } else {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications, key = { it.id }) { NotifCard(it) { onNotificationClick(it) } }
        }
    }
}

@Composable
private fun NotifCard(n: AppNotification, onClick: () -> Unit) {
    val icon = when (n.type) {
        NotificationType.QUIZ_REMINDER -> Icons.Outlined.Quiz
        NotificationType.CLASS_UPDATE -> Icons.Outlined.School
        NotificationType.ANNOUNCEMENT -> Icons.Outlined.Campaign
        NotificationType.GENERAL -> Icons.Outlined.Info
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val iconColor = when (n.type) {
        NotificationType.QUIZ_REMINDER -> tertiaryColor
        NotificationType.CLASS_UPDATE -> primaryColor
        NotificationType.ANNOUNCEMENT -> WarningAmber
        NotificationType.GENERAL -> secondaryColor
    }

    CyberCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderColor = if (!n.isRead) primaryColor.copy(alpha = 0.35f) else GlassBorderSubtle
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.12f))
                    .border(1.dp, iconColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(20.dp), tint = iconColor)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        n.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!n.isRead) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (!n.isRead) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            Modifier
                                .size(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(primaryColor)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    n.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    n.createdAt.take(10),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ─── Announcements Tab ───

@Composable
private fun AnnouncementsTab(
    announcements: List<Announcement>,
    onAnnouncementClick: (Announcement) -> Unit
) {
    if (announcements.isEmpty()) {
        EmptyStateScreen(Icons.Outlined.Campaign, "No Announcements", "Check back later!")
    } else {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(announcements, key = { it.id }) { AnnouncementCard(it) { onAnnouncementClick(it) } }
        }
    }
}

private data class AnnouncementTagStyle(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val actionText: String?
)

@Composable
private fun getAnnouncementTagStyle(tag: String, targetScreen: String?): AnnouncementTagStyle {
    val normTag = tag.trim().uppercase()
    val target = (targetScreen ?: normTag).trim().uppercase()
    return when (normTag) {
        "QUIZ" -> AnnouncementTagStyle(
            label = "Quiz Alert",
            icon = Icons.Outlined.Quiz,
            color = BrandOrange,
            actionText = if (target == "QUIZ") "Open Quizzes" else null
        )
        "ASSIGNMENT" -> AnnouncementTagStyle(
            label = "Assignment",
            icon = Icons.AutoMirrored.Outlined.Assignment,
            color = BrandCyan,
            actionText = if (target == "ASSIGNMENT") "View Assignment" else null
        )
        "SCHEDULE" -> AnnouncementTagStyle(
            label = "Schedule",
            icon = Icons.Outlined.CalendarMonth,
            color = SuccessGreen,
            actionText = if (target == "SCHEDULE") "View Schedule" else null
        )
        "URGENT" -> AnnouncementTagStyle(
            label = "Urgent",
            icon = Icons.Outlined.Warning,
            color = BrandCoral,
            actionText = null
        )
        else -> AnnouncementTagStyle(
            label = "General",
            icon = Icons.Outlined.Campaign,
            color = BrandCyanLight,
            actionText = null
        )
    }
}

@Composable
private fun AnnouncementCard(a: Announcement, onClick: () -> Unit) {
    val tagStyle = getAnnouncementTagStyle(a.tag, a.targetScreen)
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val borderColor = when (a.tag.uppercase()) {
        "URGENT" -> BrandCoral.copy(alpha = 0.5f)
        "QUIZ" -> BrandOrange.copy(alpha = 0.35f)
        "ASSIGNMENT" -> BrandCyan.copy(alpha = 0.35f)
        "SCHEDULE" -> SuccessGreen.copy(alpha = 0.35f)
        else -> GlassBorderSubtle
    }

    CyberCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderColor = borderColor
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = tagStyle.color.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, tagStyle.color.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(tagStyle.icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = tagStyle.color)
                        Text(
                            tagStyle.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = tagStyle.color
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tertiaryColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, tertiaryColor.copy(alpha = 0.25f))
                ) {
                    Text(
                        a.audience.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = tertiaryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                a.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(6.dp))

            Text(
                a.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                a.createdAt?.let {
                    Text(
                        it.take(16).replace('T', ' '),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                } ?: Spacer(Modifier.width(1.dp))

                if (tagStyle.actionText != null) {
                    Text(
                        "👉 ${tagStyle.actionText}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = tagStyle.color
                    )
                }
            }
        }
    }
}
