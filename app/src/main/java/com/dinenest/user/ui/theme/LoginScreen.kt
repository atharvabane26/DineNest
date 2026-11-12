package com.dinenest.user.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dinenest.user.data.AuthRepository

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNeedsVerification: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    fun doLogin() {
        if (identifier.isBlank() || password.isEmpty()) {
            error = "Enter your email or username and your password."
            return
        }
        focusManager.clearFocus()
        loading = true
        error = ""
        info = ""
        AuthRepository.login(identifier, password) { err, verified ->
            loading = false
            when {
                err != null -> error = err
                verified -> onLoggedIn()
                else -> onNeedsVerification()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthHeader("Welcome back")
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            label = { Text("Email or username") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Done,
            onDone = { doLogin() }
        )

        TextButton(
            onClick = {
                if (!identifier.contains("@")) {
                    info = ""
                    error = "Enter your email in the box above to reset your password."
                } else {
                    AuthRepository.sendPasswordReset(identifier) { err ->
                        if (err == null) {
                            error = ""
                            info = "Password reset email sent. Check your inbox."
                        } else {
                            info = ""
                            error = err
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot password?")
        }

        ErrorText(error)
        if (info.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(info, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { doLogin() },
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Log in")
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRegisterClick) {
            Text("New to DineNest? Create an account")
        }
    }
}