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
import androidx.compose.ui.unit.dp
import com.dinenest.user.model.FoodMall
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onMallClick: (FoodMall) -> Unit) {
    var malls by remember { mutableStateOf<List<FoodMall>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food malls ahead") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                error.isNotEmpty() -> Text(error, Modifier.padding(16.dp))
                malls.isEmpty() -> Text("No food malls found", Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(malls) { mall -> MallCard(mall, onClick = { onMallClick(mall) }) }
                }
            }
        }
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