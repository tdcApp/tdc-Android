package com.bagadbille.tdc.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.model.ClassInfo
import com.bagadbille.tdc.data.model.UserProfile
import com.bagadbille.tdc.data.model.isMentor
import androidx.compose.foundation.border
import com.bagadbille.tdc.ui.components.CyberCard
import com.bagadbille.tdc.ui.components.ErrorScreen
import com.bagadbille.tdc.ui.components.LoadingScreen
import com.bagadbille.tdc.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNotificationClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = false,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ProfileEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        com.bagadbille.tdc.ui.components.TdcHeader(
            onNotificationClick = onNotificationClick,
            hasUnreadNotifications = hasUnreadNotifications
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> LoadingScreen()
                is ProfileUiState.Error -> ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.loadProfile() }
                )
                is ProfileUiState.Success -> {
                    com.bagadbille.tdc.ui.components.TdcPullToRefresh(
                        isRefreshing = false,
                        onRefresh = { viewModel.loadProfile() }
                    ) {
                        ProfileContent(
                            data = state.data,
                            isDarkTheme = isDarkTheme,
                            onEditClick = { showEditDialog = true },
                            onToggleDarkTheme = { viewModel.toggleDarkTheme(it) },
                            onLogoutClick = { showLogoutDialog = true }
                        )
                    }

                    if (showEditDialog) {
                        EditProfileDialog(
                            currentName = state.data.profile.name ?: "",
                            currentPhone = state.data.profile.phone,
                            currentYear = state.data.profile.year,
                            currentEnrollment = state.data.profile.enrollmentNumber,
                            isStudent = !state.data.profile.isMentor,
                            isUpdating = state.data.isUpdating,
                            onDismiss = { showEditDialog = false },
                            onSave = { name, phone, year, enrollment ->
                                viewModel.updateProfile(name, phone, year, enrollment) { success ->
                                    if (success) showEditDialog = false
                                }
                            }
                        )
                    }

                    if (showLogoutDialog) {
                        AlertDialog(
                            onDismissRequest = { showLogoutDialog = false },
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            title = { Text("Log Out") },
                            text = { Text("Are you sure you want to log out of TDC? You will need to log in again to access your classes and assignments.") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showLogoutDialog = false
                                        viewModel.logout {
                                            onLogout()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Log Out")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showLogoutDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun ProfileContent(
    data: ProfileUiData,
    isDarkTheme: Boolean,
    onEditClick: () -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    onLogoutClick: () -> Unit
) {
    val profile = data.profile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // ─── Hero Profile Card ───
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            borderColor = MaterialTheme.colorScheme.outlineVariant
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top decorative curved gradient banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(BrandCyan.copy(alpha = 0.35f), BrandOrange.copy(alpha = 0.35f))
                            )
                        )
                )

                // Avatar with edit badge overlay
                val initials = (profile.name?.takeIf { it.isNotBlank() } ?: profile.email)
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .map { it.first().uppercase() }
                    .joinToString("")
                    .ifEmpty { "ST" }

                Box(
                    modifier = Modifier
                        .offset(y = (-44).dp)
                        .size(88.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(3.dp, BrandCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp),
                            color = BrandCyan,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Small edit icon button at bottom right
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(BrandCyan)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .clickable { onEditClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF071216),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Profile Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-32).dp)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = profile.name?.ifBlank { null } ?: profile.email.substringBefore("@").ifBlank { "User" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    val subtitle = buildString {
                        if (!profile.year.isNullOrBlank()) append("${profile.year} • ")
                        append(profile.role.replaceFirstChar { it.uppercase() })
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = BrandOrange
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Student • Developer • Learner\nBuilding cool things and exploring tech.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))

                    // Edit Profile Pill Button
                    com.bagadbille.tdc.ui.components.TdcSecondaryButton(
                        text = "Edit Profile",
                        icon = Icons.Outlined.Edit,
                        onClick = onEditClick,
                        modifier = Modifier.fillMaxWidth(0.55f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ─── 3 Stats Counters Row (Quizzes, Assignments, Batches) ───
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileStatCard(
                count = data.completedQuizzesCount.toString(),
                label = "Quizzes",
                accentColor = BrandCyan,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                count = data.submittedAssignmentsCount.toString(),
                label = "Assignments",
                accentColor = BrandOrange,
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                count = data.enrolledBatchesCount.toString(),
                label = "Classes",
                accentColor = BrandCoral,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ─── Skills Section ───
        Text(
            text = "Skills & Technologies",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(10.dp))

        val skillList = remember(data.languageClass, data.technologyClass) {
            val list = mutableListOf<String>()
            data.languageClass?.name?.let { list.add(it) }
            data.technologyClass?.name?.let { list.add(it) }
            if (list.isEmpty()) {
                list.addAll(listOf("Kotlin", "Android", "Jetpack Compose", "Java", "Python"))
            } else {
                list.addAll(listOf("Android", "Jetpack Compose", "Problem Solving"))
            }
            list.distinct()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            skillList.take(4).forEach { skill ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = skill,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ─── Enrolled Batches Section (students only) ───
        if (!profile.isMentor) {
            Text(
                text = "Enrolled Batches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(10.dp))

            EnrolledClassCard(
                trackLabel = "Language Track",
                classInfo = data.languageClass,
                icon = Icons.Outlined.Code,
                accentColor = BrandCyan
            )

            Spacer(Modifier.height(10.dp))

            EnrolledClassCard(
                trackLabel = "Technology Track",
                classInfo = data.technologyClass,
                icon = Icons.Outlined.Terminal,
                accentColor = BrandOrange
            )

            Spacer(Modifier.height(20.dp))
        }

        // ─── Personal Details Section ───
        Text(
            text = "Personal Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(10.dp))

        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                DetailRow(
                    icon = Icons.Outlined.Phone,
                    label = "Phone Number",
                    value = profile.phone
                )
                if (!profile.isMentor) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DetailRow(
                        icon = Icons.Outlined.Badge,
                        label = "Enrollment Number",
                        value = profile.enrollmentNumber ?: "Not set"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DetailRow(
                        icon = Icons.Outlined.CalendarMonth,
                        label = "College Year",
                        value = profile.year ?: "Not set"
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DetailRow(
                    icon = Icons.Outlined.Email,
                    label = "Email Address",
                    value = profile.email
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ─── App Preferences Section ───
        Text(
            text = "App Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(10.dp))

        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            onClick = { onToggleDarkTheme(!isDarkTheme) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                        .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                        contentDescription = "Theme",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dark Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isDarkTheme) "Dark appearance active" else "Light appearance active",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onToggleDarkTheme,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    )
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // ─── Log Out Button ───
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.45f))
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Log Out", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun EnrolledClassCard(
    trackLabel: String,
    classInfo: ClassInfo?,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color
) {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        borderColor = if (classInfo != null) accentColor.copy(alpha = 0.35f) else GlassBorderSubtle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = trackLabel,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trackLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = classInfo?.name ?: "Not enrolled in a class",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (classInfo != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (classInfo != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Enrolled",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    currentName: String,
    currentPhone: String,
    currentYear: String?,
    currentEnrollment: String? = null,
    isStudent: Boolean = true,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, year: String?, enrollment: String?) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var phone by remember { mutableStateOf(currentPhone) }
    var year by remember { mutableStateOf(currentYear) }
    var enrollmentNumber by remember { mutableStateOf(currentEnrollment ?: "") }
    var yearDropdownExpanded by remember { mutableStateOf(false) }
    val yearOptions = remember { listOf("1st Year", "2nd Year", "3rd Year", "4th Year") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        icon = {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Edit Profile") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = { Text("Full Name") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        validationError = null
                    },
                    label = { Text("Phone Number") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                if (isStudent) {
                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = enrollmentNumber,
                        onValueChange = {
                            enrollmentNumber = it
                            validationError = null
                        },
                        label = { Text("Enrollment Number") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Badge, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    Spacer(Modifier.height(14.dp))

                    val isYearLocked = !currentYear.isNullOrBlank()

                    if (isYearLocked) {
                        OutlinedTextField(
                            value = currentYear,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Academic Year") },
                            leadingIcon = {
                                Icon(Icons.Outlined.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            trailingIcon = {
                                Icon(Icons.Outlined.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            supportingText = {
                                Text("Academic year is locked and cannot be changed once set.")
                            },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = yearDropdownExpanded,
                            onExpandedChange = { yearDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = year ?: "Select Academic Year",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Academic Year *") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearDropdownExpanded) },
                                supportingText = {
                                    Text("Can only be set once.")
                                },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = yearDropdownExpanded,
                                onDismissRequest = { yearDropdownExpanded = false }
                            ) {
                                yearOptions.forEach { yr ->
                                    DropdownMenuItem(
                                        text = { Text(yr) },
                                        onClick = {
                                            year = yr
                                            yearDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                validationError?.let { err ->
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        validationError = "Name cannot be empty"
                        return@Button
                    }
                    if (phone.isBlank()) {
                        validationError = "Phone number cannot be empty"
                        return@Button
                    }
                    onSave(name, phone, if (isStudent) year else null, if (isStudent) enrollmentNumber.trim().ifBlank { null } else null)
                },
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUpdating
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ProfileStatCard(
    count: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier,
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
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                ),
                color = accentColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
