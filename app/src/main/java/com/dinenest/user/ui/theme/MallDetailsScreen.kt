package com.dinenest.user.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dinenest.user.data.SavedRepository
import com.dinenest.user.model.FoodMall

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MallDetailsScreen(mall: FoodMall, onBack: () -> Unit) {
    val savedIds = rememberSavedIds()
    val saved = mall.id in savedIds

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mall.name) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(mall.address, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Text("Rating: ${mall.rating}")
            Text(if (mall.isOpen) "Open now" else "Closed")
            Spacer(Modifier.height(16.dp))
            Text("Facilities", style = MaterialTheme.typography.titleMedium)
            Text(if (mall.hasRestRooms) "Rest rooms available" else "No rest rooms")
            Text(if (mall.hasEvCharging) "EV charging available" else "No EV charging")
            Spacer(Modifier.height(8.dp))
            SaveButton(
                saved = saved,
                onClick = {
                    if (saved) {
                        SavedRepository.unsave(mall.id)
                    } else {
                        SavedRepository.save(mall.id, "mall", mall.name, mall.address)
                    }
                }
            )
            Spacer(Modifier.height(24.dp))
            Text("Menu coming soon", style = MaterialTheme.typography.titleMedium)
        }
    }
}