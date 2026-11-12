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
import com.dinenest.user.data.Validators

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val formValid = Validators.isValidName(name) &&
            Validators.isValidUsername(username) &&
            Validators.isValidEmail(email.trim()) &&
            Validators.isValidPassword(password)

    fun doRegister() {
        focusManager.clearFocus()
        loading = true
        error = ""
        AuthRepository.register(name, username, email, password) { err ->
            loading = false
            if (err == null) onRegistered() else error = err
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
        AuthHeader("Create your account")
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { input ->
                username = input.lowercase()
                    .filter { c -> c in 'a'..'z' || c in '0'..'9' || c == '_' }
                    .take(20)
            },
            label = { Text("Username") },
            supportingText = { Text("3-20 characters: a-z, 0-9 and underscore") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email ID") },
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
            onDone = { if (formValid && !loading) doRegister() }
        )
        Spacer(Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            RuleLine(Validators.hasMinLength(password), "At least 6 characters")
            RuleLine(Validators.hasLetter(password), "At least 1 letter")
            RuleLine(Validators.hasDigit(password), "At least 1 number")
            RuleLine(Validators.hasSpecial(password), "At least 1 special character (@, #, !, ...)")
        }

        ErrorText(error)

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { doRegister() },
            enabled = formValid && !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Create account")
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onLoginClick) {
            Text("Already have an account? Log in")
        }
    }
}