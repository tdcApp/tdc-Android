package com.bagadbille.tdc.ui.mentor.quiz

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.data.model.QuizQuestionDraft
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuizScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CreateQuizViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var classDropdownExpanded by remember { mutableStateOf(false) }
    val calendar = remember { Calendar.getInstance() }

    fun showDateTimePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                        isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        val isoString = isoFormat.format(calendar.time)

                        val displayFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        val displayString = displayFormat.format(calendar.time)

                        viewModel.updateStartTime(isoString, displayString)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CreateQuizEvent.QuizSaved -> onSaved()
                is CreateQuizEvent.ShowMessage -> snackbar.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Quiz", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveQuiz() },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error banner if any
            uiState.errorMessage?.let { errorMsg ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            // Quiz Metadata Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Quiz Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            label = { Text("Quiz Title") },
                            placeholder = { Text("e.g. TDC Coding Quest Season 3") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SuggestionChip(
                                onClick = {
                                    viewModel.updateTitle("TDC Coding Quest Season 3")
                                    viewModel.updateAudience("all")
                                },
                                label = { Text("🏆 TDC Coding Quest Season 3", style = MaterialTheme.typography.labelSmall) }
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Text("Audience", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "all" to "All",
                                "language" to "Language",
                                "technology" to "Technology"
                            ).forEach { (key, label) ->
                                FilterChip(
                                    selected = uiState.audience == key,
                                    onClick = { viewModel.updateAudience(key) },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        if (uiState.audience != "all") {
                            Spacer(Modifier.height(10.dp))
                            ExposedDropdownMenuBox(
                                expanded = classDropdownExpanded,
                                onExpandedChange = { classDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = uiState.classes[uiState.classId] ?: "All ${uiState.audience} classes",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Target Class (Optional)") },
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
                                    DropdownMenuItem(
                                        text = { Text("All ${uiState.audience} classes") },
                                        onClick = {
                                            viewModel.updateClassId(null)
                                            classDropdownExpanded = false
                                        }
                                    )
                                    uiState.classes.forEach { (id, name) ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                viewModel.updateClassId(id)
                                                classDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Target Academic Year",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (uiState.targetYears.isEmpty()) "All Years" else uiState.targetYears.sorted().joinToString(", "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.targetYears.isEmpty(),
                                onClick = { viewModel.selectAllYears() },
                                label = { Text("All Years", style = MaterialTheme.typography.labelSmall) }
                            )
                            listOf("1st Year", "2nd Year", "3rd Year", "4th Year").forEach { yr ->
                                FilterChip(
                                    selected = uiState.targetYears.contains(yr),
                                    onClick = { viewModel.toggleTargetYear(yr) },
                                    label = { Text(yr, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }

            // Timing & Schedule Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Schedule & Timing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Starting Time
                        Text("Starting Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = uiState.startImmediately,
                                onClick = { viewModel.updateStartImmediately(true) },
                                label = { Text("Start Immediately", style = MaterialTheme.typography.labelSmall) }
                            )
                            FilterChip(
                                selected = !uiState.startImmediately,
                                onClick = {
                                    viewModel.updateStartImmediately(false)
                                    if (uiState.startTimeIso == null) {
                                        showDateTimePicker()
                                    }
                                },
                                label = { Text("Scheduled", style = MaterialTheme.typography.labelSmall) }
                            )
                        }

                        if (!uiState.startImmediately) {
                            Spacer(Modifier.height(10.dp))
                            OutlinedCard(
                                onClick = { showDateTimePicker() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            if (uiState.startTimeDisplay.isNotBlank()) uiState.startTimeDisplay else "Select Start Date & Time",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (uiState.startTimeDisplay.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        "Change",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Entering Window
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Entering Window",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${uiState.enteringWindowMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            "Window after start during which students are allowed to enter",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 30, 45, 60).forEach { mins ->
                                FilterChip(
                                    selected = uiState.enteringWindowMinutes == mins,
                                    onClick = { viewModel.updateEnteringWindow(mins) },
                                    label = { Text("${mins}m", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Total Duration
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total Quiz Duration",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${uiState.totalTimeMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            "Time limit for each student to complete the questions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(10, 15, 20, 30, 45, 60, 90).forEach { mins ->
                                FilterChip(
                                    selected = uiState.totalTimeMinutes == mins,
                                    onClick = { viewModel.updateTotalTime(mins) },
                                    label = { Text("${mins}m", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }

            // Questions Section Header
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Questions (${uiState.questions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedButton(onClick = { viewModel.addQuestion() }) {
                        Icon(Icons.Outlined.Add, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Question")
                    }
                }
            }

            // Question Cards
            itemsIndexed(uiState.questions) { index, question ->
                QuestionEditorCard(
                    index = index,
                    draft = question,
                    canDelete = uiState.questions.size > 1,
                    onUpdate = { updated -> viewModel.updateQuestion(index, updated) },
                    onDelete = { viewModel.removeQuestion(index) }
                )
            }

            // Bottom Add Question button
            item {
                OutlinedButton(
                    onClick = { viewModel.addQuestion() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Add, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Another Question")
                }
            }
        }
    }
}

@Composable
private fun QuestionEditorCard(
    index: Int,
    draft: QuizQuestionDraft,
    canDelete: Boolean,
    onUpdate: (QuizQuestionDraft) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        "Question ${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete Question",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = draft.question,
                onValueChange = { onUpdate(draft.copy(question = it)) },
                label = { Text("Question Prompt") },
                placeholder = { Text("e.g. What is the output of print(2 ** 3)?") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(12.dp))

            Text("Options", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))

            // Option A
            OptionTextField(
                letter = "A",
                value = draft.optionA,
                isCorrect = draft.correctOption.equals("a", ignoreCase = true),
                onValueChange = { onUpdate(draft.copy(optionA = it)) }
            )

            Spacer(Modifier.height(8.dp))

            // Option B
            OptionTextField(
                letter = "B",
                value = draft.optionB,
                isCorrect = draft.correctOption.equals("b", ignoreCase = true),
                onValueChange = { onUpdate(draft.copy(optionB = it)) }
            )

            Spacer(Modifier.height(8.dp))

            // Option C
            OptionTextField(
                letter = "C",
                value = draft.optionC,
                isCorrect = draft.correctOption.equals("c", ignoreCase = true),
                onValueChange = { onUpdate(draft.copy(optionC = it)) }
            )

            Spacer(Modifier.height(8.dp))

            // Option D
            OptionTextField(
                letter = "D",
                value = draft.optionD,
                isCorrect = draft.correctOption.equals("d", ignoreCase = true),
                onValueChange = { onUpdate(draft.copy(optionD = it)) }
            )

            Spacer(Modifier.height(14.dp))

            // Correct Option Selector
            Text("Select Correct Option", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("a" to "A", "b" to "B", "c" to "C", "d" to "D").forEach { (key, letter) ->
                    val selected = draft.correctOption.equals(key, ignoreCase = true)
                    FilterChip(
                        modifier = Modifier.weight(1f),
                        selected = selected,
                        onClick = { onUpdate(draft.copy(correctOption = key)) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Outlined.Check, null, Modifier.size(14.dp)) }
                        } else null,
                        label = {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(letter, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionTextField(
    letter: String,
    value: String,
    isCorrect: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Option $letter") },
        leadingIcon = {
            Surface(
                modifier = Modifier.size(26.dp),
                shape = CircleShape,
                color = if (isCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        letter,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    )
}
