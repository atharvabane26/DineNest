package com.dinenest.user.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dinenest.user.data.AuthRepository
import com.dinenest.user.data.Validators

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Change password", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            ChangePasswordSection()

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            Text("Change email", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            ChangeEmailSection()

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChangePasswordSection() {
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var success by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val canSubmit = current.isNotEmpty() &&
            Validators.isValidPassword(newPass) &&
            newPass == confirm && !loading

    Column(modifier = Modifier.fillMaxWidth()) {
        PasswordField(
            value = current,
            onValueChange = { current = it },
            label = "Current password",
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(
            value = newPass,
            onValueChange = { newPass = it },
            label = "New password",
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(8.dp))
        RuleLine(Validators.hasMinLength(newPass), "At least 6 characters")
        RuleLine(Validators.hasLetter(newPass), "At least 1 letter")
        RuleLine(Validators.hasDigit(newPass), "At least 1 number")
        RuleLine(Validators.hasSpecial(newPass), "At least 1 special character")
        Spacer(Modifier.height(12.dp))
        PasswordField(
            value = confirm,
            onValueChange = { confirm = it },
            label = "Confirm new password",
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Done
        )
        if (confirm.isNotEmpty() && confirm != newPass) {
            Text(
                "Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        ErrorText(error)
        if (success.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(success)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                focusManager.clearFocus()
                loading = true
                error = ""
                success = ""
                AuthRepository.changePassword(current, newPass) { err ->
                    loading = false
                    if (err == null) {
                        success = "Password changed successfully."
                        current = ""
                        newPass = ""
                        confirm = ""
                    } else {
                        error = err
                    }
                }
            },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Update password")
            }
        }
    }
}

@Composable
private fun ChangeEmailSection() {
    var password by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var success by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val canSubmit = password.isNotEmpty() &&
            Validators.isValidEmail(newEmail.trim()) && !loading

    Column(modifier = Modifier.fillMaxWidth()) {
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Current password",
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = newEmail,
            onValueChange = { newEmail = it },
            label = { Text("New email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        ErrorText(error)
        if (success.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(success)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                focusManager.clearFocus()
                loading = true
                error = ""
                success = ""
                AuthRepository.changeEmail(password, newEmail) { err ->
                    loading = false
                    if (err == null) {
                        success = "We sent a verification link to ${newEmail.trim()}. " +
                                "Your email changes after you open that link."
                        password = ""
                        newEmail = ""
                    } else {
                        error = err
                    }
                }
            },
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Update email")
            }
        }
    }
}