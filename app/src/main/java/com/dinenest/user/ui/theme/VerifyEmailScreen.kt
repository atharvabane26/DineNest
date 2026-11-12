package com.dinenest.user.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dinenest.user.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth

@Composable
fun VerifyEmailScreen(
    onVerified: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthHeader("Verify your email")
        Spacer(Modifier.height(24.dp))

        Text("We sent a verification link to", style = MaterialTheme.typography.bodyMedium)
        Text(email, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Open the link in that email, then come back here and tap the button below. " +
                    "If you can't find the email, check your spam folder.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                loading = true
                message = ""
                AuthRepository.checkVerified { ok ->
                    loading = false
                    if (ok) onVerified()
                    else message = "Your email isn't verified yet. Please open the link first."
                }
            },
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("I've verified my email")
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                AuthRepository.resendVerification { err ->
                    message = err ?: "Verification email sent again."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Resend email")
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = {
            AuthRepository.signOut()
            onBackToLogin()
        }) {
            Text("Use a different account")
        }
    }
}