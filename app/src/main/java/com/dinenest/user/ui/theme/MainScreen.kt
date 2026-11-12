package com.dinenest.user.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dinenest.user.model.FoodMall

@Composable
fun MainScreen(
    onMallClick: (FoodMall) -> Unit,
    onLogout: () -> Unit,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    val titles = listOf("Home", "Orders", "Rest & EV", "Profile")
    val icons = listOf(
        Icons.Filled.Home,
        Icons.Filled.List,
        Icons.Filled.Place,
        Icons.Filled.Person
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                titles.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
            when (tab) {
                0 -> HomeScreen(onMallClick = onMallClick)
                1 -> OrdersScreen()
                2 -> FacilitiesScreen()
                else -> ProfileScreen(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun OrdersScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No orders yet", style = MaterialTheme.typography.titleMedium)
    }
}