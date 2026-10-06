package com.bagadbille.tdc.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.ui.components.TdcButton
import com.bagadbille.tdc.ui.components.TdcTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val languageClasses by viewModel.languageClasses.collectAsStateWithLifecycle()
    val technologyClasses by viewModel.technologyClasses.collectAsStateWithLifecycle()
    val fm = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var enrollment by remember { mutableStateOf("") }
    var selectedLanguageClassId by remember { mutableStateOf<String?>(null) }
    var selectedTechnologyClassId by remember { mutableStateOf<String?>(null) }
    var langDropdownExpanded by remember { mutableStateOf(false) }
    var techDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is SetupUiState.Success) {
            onSetupComplete()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Text(
                "Complete Student Profile",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Set up your details and select your batches",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            // Notice that role is managed by Admin
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Registered as a Student. Mentor accounts and elevated roles are provisioned by TDC Administrators.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Full Name
            TdcTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                leadingIcon = Icons.Outlined.Person,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
            )

            Spacer(Modifier.height(16.dp))

            // Mobile Number
            TdcTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = "Mobile Number",
                leadingIcon = Icons.Outlined.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) })
            )

            Spacer(Modifier.height(16.dp))

            // Language Batch Dropdown
            val selectedLangName = languageClasses.firstOrNull { it.id == selectedLanguageClassId }?.name ?: "Select Language Batch"
            ExposedDropdownMenuBox(
                expanded = langDropdownExpanded,
                onExpandedChange = { langDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedLangName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Language Batch") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Code, null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langDropdownExpanded) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = langDropdownExpanded,
                    onDismissRequest = { langDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            selectedLanguageClassId = null
                            langDropdownExpanded = false
                        }
                    )
                    languageClasses.forEach { cls ->
                        DropdownMenuItem(
                            text = { Text(cls.name) },
                            onClick = {
                                selectedLanguageClassId = cls.id
                                langDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Technology Batch Dropdown
            val selectedTechName = technologyClasses.firstOrNull { it.id == selectedTechnologyClassId }?.name ?: "Select Technology Batch"
            ExposedDropdownMenuBox(
                expanded = techDropdownExpanded,
                onExpandedChange = { techDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedTechName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Technology Batch") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Terminal, null, tint = MaterialTheme.colorScheme.secondary)
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = techDropdownExpanded) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = techDropdownExpanded,
                    onDismissRequest = { techDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            selectedTechnologyClassId = null
                            techDropdownExpanded = false
                        }
                    )
                    technologyClasses.forEach { cls ->
                        DropdownMenuItem(
                            text = { Text(cls.name) },
                            onClick = {
                                selectedTechnologyClassId = cls.id
                                techDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Enrollment Number (Optional)
            TdcTextField(
                value = enrollment,
                onValueChange = { enrollment = it },
                label = "Enrollment Number (Optional)",
                leadingIcon = Icons.Outlined.Badge,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { fm.clearFocus() })
            )

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(uiState is SetupUiState.Error) {
                Text(
                    (uiState as? SetupUiState.Error)?.message ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            TdcButton(
                text = "Save & Continue to Profile",
                onClick = {
                    fm.clearFocus()
                    viewModel.completeProfile(
                        name = name,
                        languageClassId = selectedLanguageClassId,
                        technologyClassId = selectedTechnologyClassId,
                        enrollmentNumber = enrollment,
                        mobileNumber = mobileNumber
                    )
                },
                isLoading = uiState is SetupUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))
        }
    }
}
