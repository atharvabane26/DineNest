package com.dinenest.user.ui.theme

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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dinenest.user.data.SavedRepository
import com.dinenest.user.model.Facility
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilitiesScreen() {
    val types = listOf("restroom", "ev")
    val labels = listOf("Rest rooms", "EV charging")
    val savedIds = rememberSavedIds()

    var selected by remember { mutableIntStateOf(0) }
    var facilities by remember { mutableStateOf<List<Facility>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    DisposableEffect(selected) {
        loading = true
        error = ""
        val registration = FirebaseFirestore.getInstance()
            .collection("facilities")
            .whereEqualTo("type", types[selected])
            .addSnapshotListener { snapshot, e ->
                loading = false
                if (e != null) {
                    error = e.message ?: "Something went wrong"
                    return@addSnapshotListener
                }
                facilities = snapshot?.toObjects(Facility::class.java) ?: emptyList()
            }
        onDispose { registration.remove() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Rest rooms and EV") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selected) {
                labels.forEachIndexed { index, label ->
                    Tab(
                        selected = selected == index,
                        onClick = { selected = index },
                        text = { Text(label) }
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    error.isNotEmpty() -> Text(error, Modifier.padding(16.dp))
                    facilities.isEmpty() -> Text(
                        "Nothing found",
                        Modifier.align(Alignment.Center)
                    )
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(facilities) { item ->
                            val isSaved = item.id in savedIds
                            FacilityCard(
                                item = item,
                                saved = isSaved,
                                onToggleSave = {
                                    if (isSaved) {
                                        SavedRepository.unsave(item.id)
                                    } else {
                                        SavedRepository.save(
                                            item.id, item.type, item.name, item.address
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FacilityCard(item: Facility, saved: Boolean, onToggleSave: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Text(item.address, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Text(item.details)
            Text(
                if (item.type == "restroom") {
                    "Sleeps up to ${item.capacity} person(s)"
                } else {
                    "${item.capacity} charging port(s)"
                }
            )
            Text("₹${item.price.toInt()} ${item.priceUnit}")
            Text(if (item.available) "Available now" else "Not available")
            SaveButton(saved = saved, onClick = onToggleSave)
        }
    }
}