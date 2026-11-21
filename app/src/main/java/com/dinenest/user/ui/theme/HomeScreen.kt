package com.dinenest.user.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dinenest.user.model.FoodMall
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(onMallClick: (FoodMall) -> Unit) {
    var malls by remember { mutableStateOf<List<FoodMall>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var showNotifications by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val registration = FirebaseFirestore.getInstance()
            .collection("foodMalls")
            .addSnapshotListener { snapshot, e ->
                loading = false
                if (e != null) {
                    error = e.message ?: "Something went wrong"
                    return@addSnapshotListener
                }
                malls = snapshot?.toObjects(FoodMall::class.java) ?: emptyList()
            }
        onDispose { registration.remove() }
    }

    val text = query.trim()
    val visible = if (text.isEmpty()) {
        malls
    } else {
        malls.filter {
            it.name.contains(text, ignoreCase = true) ||
                    it.address.contains(text, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                query = query,
                onQueryChange = { query = it },
                onNotificationClick = { showNotifications = true }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                error.isNotEmpty() -> Text(error, Modifier.padding(16.dp))
                malls.isEmpty() -> Text(
                    "No food malls found",
                    Modifier.align(Alignment.Center)
                )
                visible.isEmpty() -> Text(
                    "No food malls match \"$text\"",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    textAlign = TextAlign.Center
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visible) { mall ->
                        MallCard(mall, onClick = { onMallClick(mall) })
                    }
                }
            }
        }
    }

    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            title = { Text("Notifications") },
            text = { Text("You have no new notifications.") },
            confirmButton = {
                TextButton(onClick = { showNotifications = false }) { Text("OK") }
            }
        )
    }
}

@Composable
fun MallCard(mall: FoodMall, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(mall.name, style = MaterialTheme.typography.titleMedium)
            Text(mall.address, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("Rating: ${mall.rating}")
            val features = listOfNotNull(
                if (mall.hasRestRooms) "Rest rooms" else null,
                if (mall.hasEvCharging) "EV charging" else null
            )
            if (features.isNotEmpty()) Text(features.joinToString(" • "))
            Text(if (mall.isOpen) "Open now" else "Closed")
        }
    }
}