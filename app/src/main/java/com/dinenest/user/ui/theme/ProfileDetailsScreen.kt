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
import com.dinenest.user.data.Validators
import com.dinenest.user.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(profile: UserProfile, onBack: () -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    val focusManager = LocalFocusManager.current

    var name by remember(profile) {
        mutableStateOf(profile.name.ifEmpty { user?.displayName ?: "" })
    }
    var phone by remember(profile) { mutableStateOf(profile.phone) }
    var home by remember(profile) { mutableStateOf(profile.homeLocation) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val phoneOk = phone.isEmpty() || phone.length == 10
    val canSave = Validators.isValidName(name) && phoneOk && !loading

    fun save() {
        val u = user ?: return
        focusManager.clearFocus()
        loading = true
        message = ""
        val data = mapOf(
            "name" to name.trim(),
            "phone" to phone,
            "homeLocation" to home.trim()
        )
        FirebaseFirestore.getInstance().collection("users").document(u.uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                u.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
                )
                loading = false
                isError = false
                message = "Profile updated."
            }
            .addOnFailureListener {
                loading = false
                isError = true
                message = it.message ?: "Could not save. Please try again."
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile details") },
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
                value = profile.username,
                onValueChange = {},
                label = { Text("Username") },
                singleLine = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = user?.email ?: profile.email,
                onValueChange = {},
                label = { Text("Email (change it in Settings)") },
                singleLine = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                label = { Text("Mobile number (optional)") },
                prefix = { Text("+91 ") },
                supportingText = {
                    Text(if (phoneOk) "You can leave this empty" else "Enter all 10 digits")
                },
                isError = !phoneOk,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = home,
                onValueChange = { home = it },
                label = { Text("Home location (optional)") },
                supportingText = { Text("For example: Kothrud, Pune") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            if (message.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    message,
                    color = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { save() },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save changes")
                }
            }
        }
    }
}