package com.bagadbille.tdc.ui.mentor.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.model.UserProfile
import com.bagadbille.tdc.data.remote.dto.TeacherUserDto
import com.bagadbille.tdc.ui.components.CyberCard
import com.bagadbille.tdc.ui.components.ErrorScreen
import com.bagadbille.tdc.ui.components.LoadingScreen
import com.bagadbille.tdc.ui.theme.BrandCyan
import com.bagadbille.tdc.ui.theme.BrandOrange

@Composable
fun RosterTab(viewModel: RosterViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val teachers by viewModel.teachers.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is RosterUiState.Loading -> LoadingScreen("Loading classes...")
        is RosterUiState.Error -> ErrorScreen(s.message, onRetry = { viewModel.load() })
        is RosterUiState.Success -> RosterNavigationContainer(
            batches = s.batches,
            isAdmin = isAdmin,
            teachers = teachers,
            onLoadStudents = { viewModel.loadStudentsForBatch(it) },
            onCreateBatch = { name, type, mentorUid, cb -> viewModel.createClass(name, type, mentorUid, cb) },
            onUpdateBatch = { id, name, type, mentorUid, cb -> viewModel.updateClass(id, name, type, mentorUid, cb) },
            onDeleteBatch = { id, cb -> viewModel.deleteClass(id, cb) },
            onRefresh = { viewModel.load() }
        )
    }
}

@Composable
private fun RosterNavigationContainer(
    batches: List<BatchItem>,
    isAdmin: Boolean,
    teachers: List<TeacherUserDto>,
    onLoadStudents: (String) -> Unit,
    onCreateBatch: (String, String, String, (Boolean, String?) -> Unit) -> Unit,
    onUpdateBatch: (String, String, String, String, (Boolean, String?) -> Unit) -> Unit,
    onDeleteBatch: (String, (Boolean, String?) -> Unit) -> Unit,
    onRefresh: () -> Unit
) {
    var selectedBatchId by remember { mutableStateOf<String?>(null) }
    var editingBatch by remember { mutableStateOf<ClassInfo?>(null) }
    var showAddBatchDialog by remember { mutableStateOf(false) }

    // Find the currently selected batch if any
    val currentBatch = remember(selectedBatchId, batches) {
        batches.find { it.classInfo.id == selectedBatchId }
    }

    LaunchedEffect(selectedBatchId) {
        selectedBatchId?.let { onLoadStudents(it) }
    }

    // Intercept back button press when viewing class roster
    BackHandler(enabled = currentBatch != null) {
        selectedBatchId = null
    }

    if (currentBatch != null) {
        ClassRosterDetailScreen(
            batch = currentBatch,
            onBack = { selectedBatchId = null }
        )
    } else {
        ClassesListScreen(
            batches = batches,
            isAdmin = isAdmin,
            onBatchClick = { batch ->
                selectedBatchId = batch.classInfo.id
                onLoadStudents(batch.classInfo.id)
            },
            onAddBatchClick = { showAddBatchDialog = true },
            onEditBatchClick = { batch -> editingBatch = batch.classInfo }
        )
    }

    if (showAddBatchDialog) {
        EditBatchDialog(
            initialClass = null,
            teachers = teachers,
            onDismiss = { showAddBatchDialog = false },
            onSave = { name, type, mentorUid ->
                onCreateBatch(name, type, mentorUid) { success, _ ->
                    if (success) showAddBatchDialog = false
                }
            }
        )
    }

    editingBatch?.let { cls ->
        EditBatchDialog(
            initialClass = cls,
            teachers = teachers,
            onDismiss = { editingBatch = null },
            onSave = { name, type, mentorUid ->
                onUpdateBatch(cls.id, name, type, mentorUid) { success, _ ->
                    if (success) editingBatch = null
                }
            },
            onDelete = { id ->
                onDeleteBatch(id) { success, _ ->
                    if (success) editingBatch = null
                }
            }
        )
    }
}

/**
 * Screen 1: Shows all batches/classes in modern cards.
 * Clicking a card opens the dedicated ClassRosterDetailScreen.
 */
@Composable
private fun ClassesListScreen(
    batches: List<BatchItem>,
    isAdmin: Boolean,
    onBatchClick: (BatchItem) -> Unit,
    onAddBatchClick: () -> Unit,
    onEditBatchClick: (BatchItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(bottom = 4.dp)) {
                    Text(
                        text = "Enrolled Classes",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Select a class to view all enrolled students and their details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isAdmin) {
                    TextButton(onClick = onAddBatchClick) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandCyan)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Batch", style = MaterialTheme.typography.labelMedium, color = BrandCyan)
                    }
                }
            }
        }

        if (batches.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "No Classes Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "No classes are registered currently.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        items(batches, key = { it.classInfo.id }) { batch ->
            ClassOverviewCard(
                batch = batch,
                isAdmin = isAdmin,
                onClick = { onBatchClick(batch) },
                onEditClick = { onEditBatchClick(batch) }
            )
        }
    }
}

@Composable
private fun ClassOverviewCard(
    batch: BatchItem,
    isAdmin: Boolean = false,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {}
) {
    val isLanguage = batch.classInfo.type == "language"
    val accentColor = if (isLanguage) BrandCyan else BrandOrange

    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        borderColor = accentColor.copy(alpha = 0.25f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Class Track Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLanguage) Icons.Outlined.Code else Icons.Outlined.Terminal,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            // Info Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = batch.classInfo.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Track Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${batch.classInfo.type.replaceFirstChar { it.uppercase() }} Track",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    // Student Count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.People,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val count = if (batch.students.isNotEmpty()) batch.students.size else batch.classInfo.studentCount
                        Text(
                            text = "$count students",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Teacher contact info
                if (!batch.classInfo.mentorName.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Teacher: ${batch.classInfo.mentorName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!batch.classInfo.mentorPhone.isNullOrBlank()) {
                            Text(
                                text = "• ${batch.classInfo.mentorPhone}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            if (isAdmin) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Class",
                        tint = BrandCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                // Arrow right
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "View Students",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Screen 2: Dedicated Class Roster Screen with real-time Search Bar
 * and detailed student cards (phone number, year, enrollment number, email).
 */
@Composable
private fun ClassRosterDetailScreen(
    batch: BatchItem,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val isLanguage = batch.classInfo.type == "language"
    val accentColor = if (isLanguage) BrandCyan else BrandOrange

    val query = searchQuery.trim().lowercase()
    val filteredStudents = remember(searchQuery, batch.students) {
        if (query.isBlank()) {
            batch.students
        } else {
            batch.students.filter { student ->
                val nameMatch = student.name?.lowercase()?.contains(query) == true
                val emailMatch = student.email.lowercase().contains(query)
                val phoneMatch = student.phone.lowercase().contains(query)
                val enrollmentMatch = student.enrollmentNumber?.lowercase()?.contains(query) == true
                val yearMatch = student.year?.lowercase()?.contains(query) == true
                nameMatch || emailMatch || phoneMatch || enrollmentMatch || yearMatch
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ─── Header with Back Button ───
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to Classes",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = batch.classInfo.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val enrolledCount = if (batch.students.isNotEmpty()) batch.students.size else batch.classInfo.studentCount
                        Text(
                            text = "$enrolledCount Enrolled • ${batch.classInfo.type.replaceFirstChar { it.uppercase() }} Track",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search by name, enrollment, phone, year...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ─── Students List ───
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Count indicator
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val totalDisplayCount = if (batch.students.isNotEmpty()) batch.students.size else batch.classInfo.studentCount
                    Text(
                        text = if (query.isBlank()) "All Students ($totalDisplayCount)"
                        else "Matching Students (${filteredStudents.size})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (batch.isLoadingStudents) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = accentColor
                        )
                    }
                }
            } else if (batch.students.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonOff,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "No Students Enrolled",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "No students have been enrolled in this batch yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else if (filteredStudents.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "No students found",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "No results match \"$searchQuery\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { searchQuery = "" },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Clear Search")
                        }
                    }
                }
            } else {
                items(filteredStudents, key = { it.id }) { student ->
                    StudentDetailCard(student = student)
                }
            }
        }
    }
}

@Composable
private fun StudentDetailCard(student: UserProfile) {
    val initials = (student.name?.takeIf { it.isNotBlank() } ?: student.email)
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")
        .ifEmpty { "ST" }

    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        borderColor = MaterialTheme.colorScheme.outlineVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Avatar + Name + Enrollment Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with gradient
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(BrandCyan.copy(alpha = 0.25f), BrandOrange.copy(alpha = 0.25f))
                            )
                        )
                        .border(1.5.dp, BrandCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name?.ifBlank { null } ?: student.email.substringBefore("@"),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = student.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(10.dp))

            // Details Grid: Enrollment Number, Year, Phone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Enrollment Number Chip
                val enrollment = student.enrollmentNumber?.ifBlank { null } ?: "Not Set"
                DetailInfoChip(
                    icon = Icons.Outlined.Badge,
                    label = "Enrollment",
                    value = enrollment,
                    accentColor = BrandCyan,
                    modifier = Modifier.weight(1.3f)
                )

                // Year Chip
                val year = student.year?.ifBlank { null } ?: "Not Set"
                DetailInfoChip(
                    icon = Icons.Outlined.School,
                    label = "Year",
                    value = year,
                    accentColor = BrandOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Phone row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = "Phone",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Phone: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = student.phone.ifBlank { "Not provided" },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DetailInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBatchDialog(
    initialClass: ClassInfo? = null,
    teachers: List<TeacherUserDto>,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, mentorUid: String) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialClass?.name ?: "") }
    var type by remember { mutableStateOf(initialClass?.type?.uppercase() ?: "LANGUAGE") }
    var selectedMentorUid by remember { mutableStateOf(initialClass?.mentorUid ?: teachers.firstOrNull()?.effectiveUid ?: "") }
    var teacherDropdownExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val selectedTeacher = teachers.find { it.effectiveUid == selectedMentorUid }
    val selectedTeacherName = if (selectedTeacher != null) {
        "${selectedTeacher.name ?: "Unknown"} (${selectedTeacher.effectiveMobile})"
    } else {
        "Select Teacher"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialClass != null) "Edit Batch" else "Add New Batch") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Batch / Subject Name") },
                    placeholder = { Text("e.g. C++, Python, AI/ML") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                Text("Track Type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "LANGUAGE",
                        onClick = { type = "LANGUAGE" },
                        label = { Text("Language") }
                    )
                    FilterChip(
                        selected = type == "TECHNOLOGY",
                        onClick = { type = "TECHNOLOGY" },
                        label = { Text("Technology") }
                    )
                }

                Spacer(Modifier.height(14.dp))

                ExposedDropdownMenuBox(
                    expanded = teacherDropdownExpanded,
                    onExpandedChange = { teacherDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedTeacherName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Teacher *") },
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherDropdownExpanded) },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = teacherDropdownExpanded,
                        onDismissRequest = { teacherDropdownExpanded = false }
                    ) {
                        teachers.forEach { teacher ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(teacher.name ?: "Teacher (${teacher.email})", fontWeight = FontWeight.SemiBold)
                                        Text("${teacher.email} • ${teacher.effectiveMobile}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedMentorUid = teacher.effectiveUid
                                    teacherDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Batch name cannot be empty"
                        return@Button
                    }
                    if (selectedMentorUid.isBlank()) {
                        error = "Please select an assigned teacher"
                        return@Button
                    }
                    onSave(name.trim(), type, selectedMentorUid)
                }
            ) {
                Text(if (initialClass != null) "Save Changes" else "Create Batch")
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (initialClass != null && onDelete != null) {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )

    if (showDeleteConfirmDialog && initialClass != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Batch") },
            text = {
                Text(
                    "Are you sure you want to permanently delete \"${initialClass.name}\"?\n\nAll student enrollments, schedules, assignments, and test results linked to this batch will be permanently removed. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete(initialClass.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
