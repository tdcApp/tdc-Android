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

import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bagadbille.tdc.ui.components.TdcPullToRefresh

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                    IconButton(onClick = { viewModel.markAllAsRead() }) {
                        Icon(Icons.Outlined.DoneAll, contentDescription = "Mark all as read", tint = MaterialTheme.colorScheme.primary)
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
                        onMarkAsRead = { viewModel.markAsRead(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsContent(data: NotificationsData, onMarkAsRead: (String) -> Unit) {
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
        AlertDialog(
            onDismissRequest = { selectedAnnouncement = null },
            icon = { Icon(Icons.Outlined.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(ann.title) },
            text = {
                Column {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "Audience: ${ann.audience.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(ann.content, style = MaterialTheme.typography.bodyMedium)
                    ann.createdAt?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            it.take(16).replace('T', ' '),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAnnouncement = null }) { Text("Close") }
            }
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
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (!n.isRead) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            if (!n.isRead) {
                Box(Modifier.size(8.dp).offset(y = 6.dp).background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small))
                Spacer(Modifier.width(8.dp))
            }
            Icon(icon, null, Modifier.size(24.dp), tint = if (!n.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(n.title, style = MaterialTheme.typography.titleSmall, fontWeight = if (!n.isRead) FontWeight.SemiBold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(n.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(n.createdAt.take(10), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
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

@Composable
private fun AnnouncementCard(a: Announcement, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    a.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                    Text(
                        a.audience.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                a.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            a.createdAt?.let {
                Text(it.take(10), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    }
}
