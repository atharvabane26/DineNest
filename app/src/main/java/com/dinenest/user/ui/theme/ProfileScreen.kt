package com.dinenest.user.ui.theme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dinenest.user.data.AuthRepository
import com.dinenest.user.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private enum class ProfilePage { MENU, DETAILS, SAVED, SETTINGS }

@Composable
fun ProfileScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    var profile by remember { mutableStateOf(UserProfile()) }
    var page by remember { mutableStateOf(ProfilePage.MENU) }

    DisposableEffect(Unit) {
        AuthRepository.refreshAndSync()
        val uid = user?.uid
        val registration = if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    profile = snapshot?.toObject(UserProfile::class.java) ?: UserProfile()
                }
        } else null
        onDispose { registration?.remove() }
    }

    BackHandler(enabled = page != ProfilePage.MENU) { page = ProfilePage.MENU }

    when (page) {
        ProfilePage.MENU -> ProfileMenu(
            name = profile.name.ifEmpty { user?.displayName ?: "" },
            username = profile.username,
            email = user?.email ?: profile.email,
            phone = profile.phone,
            home = profile.homeLocation,
            darkMode = darkMode,
            onDarkModeChange = onDarkModeChange,
            onDetails = { page = ProfilePage.DETAILS },
            onSaved = { page = ProfilePage.SAVED },
            onSettings = { page = ProfilePage.SETTINGS },
            onLogout = onLogout
        )
        ProfilePage.DETAILS -> ProfileDetailsScreen(
            profile = profile,
            onBack = { page = ProfilePage.MENU }
        )
        ProfilePage.SAVED -> SavedScreen(onBack = { page = ProfilePage.MENU })
        ProfilePage.SETTINGS -> SettingsScreen(onBack = { page = ProfilePage.MENU })
    }
}

@Composable
private fun ProfileMenu(
    name: String,
    username: String,
    email: String,
    phone: String,
    home: String,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onDetails: () -> Unit,
    onSaved: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initialsOf(name),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    name.ifEmpty { "DineNest user" },
                    style = MaterialTheme.typography.headlineSmall
                )
                if (username.isNotEmpty()) {
                    Text("@$username", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        InfoRow("Phone", phone.ifEmpty { "Not added" })
        InfoRow("Mail", email)
        if (home.isNotEmpty()) InfoRow("Home", home)

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))

        MenuRow(
            icon = AppIcons.DarkMode,
            title = "Dark mode",
            onClick = { onDarkModeChange(!darkMode) }
        ) {
            Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
        }
        MenuRow(icon = Icons.Filled.Person, title = "Profile details", onClick = onDetails)
        MenuRow(icon = Icons.Filled.Favorite, title = "Saved", onClick = onSaved)
        MenuRow(icon = Icons.Filled.Settings, title = "Settings", onClick = onSettings)
        MenuRow(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            title = "Log out",
            onClick = onLogout,
            showArrow = false
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    showArrow: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                trailing()
            } else if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
    }
}

private fun initialsOf(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}