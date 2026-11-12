package com.dinenest.user.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.dinenest.user.data.SavedRepository
import com.dinenest.user.model.SavedItem
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon

@Composable
fun rememberSavedIds(): Set<String> {
    var ids by remember { mutableStateOf<Set<String>>(emptySet()) }
    DisposableEffect(Unit) {
        val registration = SavedRepository.listen { list ->
            ids = list.map { it.id }.toSet()
        }
        onDispose { registration?.remove() }
    }
    return ids
}

@Composable
fun SaveButton(saved: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Icon(
            imageVector = if (saved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(if (saved) "Saved" else "Save")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(onBack: () -> Unit) {
    var saved by remember { mutableStateOf<List<SavedItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val registration = SavedRepository.listen { list ->
            saved = list
            loading = false
        }
        if (registration == null) loading = false
        onDispose { registration?.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                saved.isEmpty() -> Text(
                    "Nothing saved yet.\nTap Save on a food mall, rest room or EV station.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    textAlign = TextAlign.Center
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(saved) { item -> SavedCard(item) }
                }
            }
        }
    }
}

@Composable
private fun SavedCard(item: SavedItem) {
    val label = when (item.type) {
        "mall" -> "Food mall"
        "restroom" -> "Rest room"
        "ev" -> "EV charging"
        else -> "Saved"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Text(item.address, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { SavedRepository.unsave(item.id) }) {
                Text("Remove")
            }
        }
    }
}