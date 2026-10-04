package com.bagadbille.tdc.ui.mentor.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.model.Announcement
import com.bagadbille.tdc.data.model.Schedule
import com.bagadbille.tdc.ui.components.ErrorScreen
import com.bagadbille.tdc.ui.components.LoadingScreen
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

private val DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun OverviewTab(
    onCreateAssignment: () -> Unit = {},
    onCreateQuiz: () -> Unit = {},
    viewModel: OverviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showPostDialog by remember { mutableStateOf(false) }
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var deleteAnnouncementTarget by remember { mutableStateOf<String?>(null) }
    var deleteScheduleTarget by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is OverviewEvent.ShowMessage -> snackbar.showSnackbar(event.message)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        when (val s = uiState) {
            is OverviewUiState.Loading -> LoadingScreen()
            is OverviewUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.load() })
            is OverviewUiState.Success -> OverviewContent(
                data = s.data,
                onCreateAssignment = onCreateAssignment,
                onCreateQuiz = onCreateQuiz,
                onPostClick = { showPostDialog = true },
                onAddScheduleClick = { showAddScheduleDialog = true },
                onDeleteAnnouncement = { deleteAnnouncementTarget = it },
                onDeleteSchedule = { deleteScheduleTarget = it }
            )
        }
        SnackbarHost(
            snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    if (showPostDialog) {
        PostAnnouncementDialog(
            isPosting = (uiState as? OverviewUiState.Success)?.data?.isPostingAnnouncement == true,
            classes = (uiState as? OverviewUiState.Success)?.data?.classNames ?: emptyMap(),
            onDismiss = { showPostDialog = false },
            onPost = { title, content, audience, classId ->
                viewModel.postAnnouncement(title, content, audience, classId)
                showPostDialog = false
            }
        )
    }

    if (showAddScheduleDialog) {
        AddScheduleDialog(
            classes = (uiState as? OverviewUiState.Success)?.data?.classNames ?: emptyMap(),
            onDismiss = { showAddScheduleDialog = false },
            onAdd = { classId, dayOfWeek, startTime, endTime, subject, room ->
                viewModel.createSchedule(classId, dayOfWeek, startTime, endTime, subject, room)
                showAddScheduleDialog = false
            }
        )
    }

    deleteAnnouncementTarget?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteAnnouncementTarget = null },
            title = { Text("Delete Announcement") },
            text = { Text("This announcement will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAnnouncement(id)
                        deleteAnnouncementTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteAnnouncementTarget = null }) { Text("Cancel") }
            }
        )
    }

    deleteScheduleTarget?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteScheduleTarget = null },
            title = { Text("Remove Schedule") },
            text = { Text("Are you sure you want to remove this scheduled class timing?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSchedule(id)
                        deleteScheduleTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { deleteScheduleTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun OverviewContent(
    data: OverviewUiData,
    onCreateAssignment: () -> Unit,
    onCreateQuiz: () -> Unit,
    onPostClick: () -> Unit,
    onAddScheduleClick: () -> Unit,
    onDeleteAnnouncement: (String) -> Unit,
    onDeleteSchedule: (String) -> Unit
) {
    val todayDow = LocalDate.now().dayOfWeek.value // 1=Mon … 7=Sun
    var selectedDayIndex by remember { mutableIntStateOf(0) } // 0 = Today, 1..7 = Mon..Sun

    val displayedSchedules = remember(selectedDayIndex, data.weekSchedules, data.todaySchedules) {
        if (selectedDayIndex == 0) {
            data.todaySchedules
        } else {
            data.weekSchedules.filter { it.dayOfWeek == selectedDayIndex }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Quick Stats ──
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.CalendarMonth,
                    value = "${data.todaySchedules.size}",
                    label = "Today's classes",
                    color = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    value = "${data.pendingSubmissions}",
                    label = "Pending reviews",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // ── Quick Actions / Shortcuts (Phase 6) ──
        item {
            Column {
                SectionTitle("Quick Actions")
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Outlined.Assignment,
                        label = "New Assignment",
                        onClick = onCreateAssignment
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Quiz,
                        label = "New Quiz",
                        onClick = onCreateQuiz
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Outlined.Campaign,
                        label = "Announce",
                        onClick = onPostClick
                    )
                }
            }
        }

        // ── Weekly Schedule Section (Phase 2) ──
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Class Schedule")
                TextButton(onClick = onAddScheduleClick) {
                    Icon(Icons.Outlined.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Timing", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Schedule day filter chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedDayIndex == 0,
                        onClick = { selectedDayIndex = 0 },
                        label = { Text("Today", style = MaterialTheme.typography.labelSmall) }
                    )
                }
                items(DAYS.indices.toList()) { index ->
                    val dow = index + 1
                    val isToday = dow == todayDow
                    FilterChip(
                        selected = selectedDayIndex == dow,
                        onClick = { selectedDayIndex = dow },
                        label = {
                            Text(
                                if (isToday) "${DAYS[index]} (Today)" else DAYS[index],
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }
        }

        if (displayedSchedules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        if (selectedDayIndex == 0) "No classes scheduled today" else "No classes scheduled on ${DAYS[selectedDayIndex - 1]}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(displayedSchedules, key = { it.id }) { schedule ->
                ScheduleChip(
                    schedule = schedule,
                    classNames = data.classNames,
                    onDelete = { onDeleteSchedule(schedule.id) }
                )
            }
        }

        // ── Announcements (Phase 3) ──
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Announcements")
                TextButton(onClick = onPostClick) {
                    Icon(Icons.Outlined.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Post", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (data.announcements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        "No announcements yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(data.announcements, key = { it.id }) { announcement ->
                AnnouncementCard(
                    announcement = announcement,
                    onDelete = { onDeleteAnnouncement(announcement.id) }
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ScheduleChip(
    schedule: Schedule,
    classNames: Map<String, String>,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Column(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        schedule.startTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        schedule.endTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    schedule.subject.ifBlank { classNames[schedule.classId] ?: schedule.classId },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        classNames[schedule.classId] ?: schedule.classId,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    schedule.room?.let {
                        Text("· $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Remove schedule",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AnnouncementCard(announcement: Announcement, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    announcement.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    announcement.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (announcement.audience != "all") {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            announcement.audience.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScheduleDialog(
    classes: Map<String, String>,
    onDismiss: () -> Unit,
    onAdd: (classId: String, dayOfWeek: Int, startTime: String, endTime: String, subject: String, room: String?) -> Unit
) {
    var selectedClassId by remember { mutableStateOf(classes.keys.firstOrNull() ?: "") }
    var classDropdownExpanded by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableIntStateOf(1) } // 1=Mon
    var subject by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("20:00") }
    var endTime by remember { mutableStateOf("21:00") }
    var room by remember { mutableStateOf("Google Meet") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Add Class Schedule") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                // Class Selector
                ExposedDropdownMenuBox(
                    expanded = classDropdownExpanded,
                    onExpandedChange = { classDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = classes[selectedClassId] ?: "Select Class",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Batch / Class") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(classDropdownExpanded) },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = classDropdownExpanded,
                        onDismissRequest = { classDropdownExpanded = false }
                    ) {
                        classes.forEach { (id, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedClassId = id
                                    classDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Day selector
                Text("Day of Week", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(DAYS.indices.toList()) { index ->
                        val dow = index + 1
                        FilterChip(
                            selected = selectedDay == dow,
                            onClick = { selectedDay = dow },
                            label = { Text(DAYS[index], style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it; error = null },
                    label = { Text("Topic / Subject") },
                    placeholder = { Text("e.g. C++ STL & Templates") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it; error = null },
                        label = { Text("Start Time") },
                        placeholder = { Text("20:00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it; error = null },
                        label = { Text("End Time") },
                        placeholder = { Text("21:00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Platform / Link") },
                    placeholder = { Text("e.g. Google Meet, Discord, Lab 3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                error?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        selectedClassId.isBlank() -> error = "Please select a class"
                        subject.isBlank() -> error = "Please enter subject/topic"
                        startTime.isBlank() || endTime.isBlank() -> error = "Please enter start and end time"
                        else -> onAdd(selectedClassId, selectedDay, startTime.trim(), endTime.trim(), subject.trim(), room.trim().ifBlank { null })
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostAnnouncementDialog(
    isPosting: Boolean,
    classes: Map<String, String>,
    onDismiss: () -> Unit,
    onPost: (title: String, content: String, audience: String, classId: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("all") }
    var selectedClassId by remember { mutableStateOf<String?>(null) }
    var classDropdownExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val audienceOptions = listOf("all" to "All members", "language" to "Language batch", "technology" to "Technology batch")

    AlertDialog(
        onDismissRequest = { if (!isPosting) onDismiss() },
        icon = { Icon(Icons.Outlined.Campaign, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Post Announcement") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("Title") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it; error = null },
                    label = { Text("Message") },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(Modifier.height(12.dp))
                // Audience chips
                Text("Audience", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    audienceOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = audience == key,
                            onClick = { audience = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                // Optional class selector
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = classDropdownExpanded,
                    onExpandedChange = { classDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = classes[selectedClassId] ?: "All classes",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Class (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(classDropdownExpanded) },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = classDropdownExpanded, onDismissRequest = { classDropdownExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("All classes") },
                            onClick = { selectedClassId = null; classDropdownExpanded = false }
                        )
                        classes.forEach { (id, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = { selectedClassId = id; classDropdownExpanded = false }
                            )
                        }
                    }
                }
                error?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        title.isBlank() -> error = "Title is required"
                        content.isBlank() -> error = "Message is required"
                        else -> onPost(title.trim(), content.trim(), audience, selectedClassId)
                    }
                },
                enabled = !isPosting
            ) {
                if (isPosting) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isPosting) { Text("Cancel") }
        }
    )
}
