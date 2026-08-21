package com.bagadbille.tdc.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bagadbille.tdc.ui.components.TdcButton
import com.bagadbille.tdc.ui.components.TdcTextField

@Composable
fun SignupScreen(onNavigateToLogin: () -> Unit, onNavigateToMain: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fm = LocalFocusManager.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var pwVisible by remember { mutableStateOf(false) }
    var cpwVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) { if (uiState is AuthUiState.Success) onNavigateToMain() }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))
            Text("TDC", style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 6.sp), color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text("Create Account", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("Sign up to get started", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(40.dp))

            TdcTextField(name, { name = it }, "Full Name", leadingIcon = Icons.Outlined.Person,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) }))
            Spacer(Modifier.height(16.dp))
            TdcTextField(email, { email = it }, "Email", leadingIcon = Icons.Outlined.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) }))
            Spacer(Modifier.height(16.dp))
            TdcTextField(password, { password = it }, "Password", leadingIcon = Icons.Outlined.Lock,
                trailingIcon = { IconButton(onClick = { pwVisible = !pwVisible }) { Icon(if (pwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Toggle") } },
                visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { fm.moveFocus(FocusDirection.Down) }))
            Spacer(Modifier.height(16.dp))
            TdcTextField(confirmPassword, { confirmPassword = it }, "Confirm Password", leadingIcon = Icons.Outlined.Lock,
                trailingIcon = { IconButton(onClick = { cpwVisible = !cpwVisible }) { Icon(if (cpwVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Toggle") } },
                visualTransformation = if (cpwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { fm.clearFocus(); viewModel.signUpWithEmail(name, email, password, confirmPassword) }))
            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(uiState is AuthUiState.Error) {
                Text((uiState as? AuthUiState.Error)?.message ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 4.dp))
            }
            Spacer(Modifier.height(24.dp))
            TdcButton("Sign Up", { viewModel.signUpWithEmail(name, email, password, confirmPassword) }, isLoading = uiState is AuthUiState.Loading, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.weight(1f))
            Row(Modifier.padding(vertical = 16.dp)) {
                Text("Already have an account? ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Sign In", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { onNavigateToLogin() })
            }
        }
    }
}
