package com.dinenest.user.ui.theme

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dinenest.user.model.Facility
import com.dinenest.user.model.FoodMall
import com.google.firebase.firestore.FirebaseFirestore

private const val HOTEL_CAPACITY = 5

private enum class FacilityFilter(val label: String, val type: String?) {
    ALL("All", null),
    RESTROOM("Restroom", "restroom"),
    EV("EV", "ev"),
    HOTEL("Hotel + Room", null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilitiesScreen() {
    val focusManager = LocalFocusManager.current

    var all by remember { mutableStateOf<List<Facility>>(emptyList()) }
    var malls by remember { mutableStateOf<List<FoodMall>>(emptyList()) }
    var loadingFacilities by remember { mutableStateOf(true) }
    var loadingMalls by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(FacilityFilter.ALL) }
    var searchFocused by remember { mutableStateOf(false) }
    var showLocationInfo by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val facilitiesReg = db.collection("facilities")
            .addSnapshotListener { snapshot, e ->
                loadingFacilities = false
                if (e != null) {
                    error = e.message ?: "Something went wrong"
                    return@addSnapshotListener
                }
                all = snapshot?.toObjects(Facility::class.java) ?: emptyList()
            }
        val mallsReg = db.collection("foodMalls")
            .addSnapshotListener { snapshot, e ->
                loadingMalls = false
                if (e != null) {
                    error = e.message ?: "Something went wrong"
                    return@addSnapshotListener
                }
                malls = snapshot?.toObjects(FoodMall::class.java) ?: emptyList()
            }
        onDispose {
            facilitiesReg.remove()
            mallsReg.remove()
        }
    }

    BackHandler(enabled = searchFocused) { focusManager.clearFocus() }

    val loading = loadingFacilities || loadingMalls
    val restrooms = all.filter { it.type == "restroom" }
    val evStations = all.filter { it.type == "ev" }
    val hotels = malls.filter { it.hasRestRooms }

    val text = query.trim()

    val visibleFacilities = when (filter) {
        FacilityFilter.HOTEL -> emptyList()
        else -> all.filter { facility ->
            (filter.type == null || facility.type == filter.type) &&
                    (text.isEmpty() ||
                            facility.name.contains(text, ignoreCase = true) ||
                            facility.address.contains(text, ignoreCase = true) ||
                            facility.details.contains(text, ignoreCase = true))
        }
    }

    // Hotel restroom details and the 12-hour price are read from foodMalls.
    val visibleHotels = when (filter) {
        FacilityFilter.EV -> emptyList()
        else -> hotels.filter { hotel ->
            text.isEmpty() ||
                    hotel.name.contains(text, ignoreCase = true) ||
                    hotel.address.contains(text, ignoreCase = true) ||
                    hotel.roomDetails.contains(text, ignoreCase = true)
        }
    }

    val total = visibleFacilities.size + visibleHotels.size

    val heading = when (filter) {
        FacilityFilter.ALL -> "All stops ($total)"
        FacilityFilter.RESTROOM -> "Restroom ($total)"
        FacilityFilter.EV -> "EV charging ($total)"
        FacilityFilter.HOTEL -> "Hotel + Room ($total)"
    }

    val cheapestRest = restrooms.minByOrNull { it.price }
    val cheapestHotelRestroom = hotels.filter { it.roomPrice > 0.0 }.minByOrNull { it.roomPrice }
    val restStat = when {
        cheapestHotelRestroom != null ->
            "${hotels.size} hotel restroom(s) • From ₹${cheapestHotelRestroom.roomPrice.toInt()} / 12 hours"
        cheapestRest != null ->
            "${restrooms.size} rest room(s) • From ₹${cheapestRest.price.toInt()} ${cheapestRest.priceUnit}"
        else -> "No rest rooms added yet"
    }

    val cheapestEv = evStations.minByOrNull { it.price }
    val evStat = if (cheapestEv == null) {
        "No EV stations added yet"
    } else {
        "${evStations.size} station(s) • ${evStations.sumOf { it.capacity }} ports • " +
                "From ₹${cheapestEv.price.toInt()} ${cheapestEv.priceUnit}"
    }

    val cheapestHotel = hotels
        .filter { it.roomPrice > 0.0 }
        .minByOrNull { it.roomPrice }
    val hotelStat = when {
        hotels.isEmpty() -> "No hotels with rooms yet"
        cheapestHotel == null -> "${hotels.size} hotel(s) • Room rates coming soon"
        else -> "${hotels.size} hotel(s) • From ₹${cheapestHotel.roomPrice.toInt()} / 12 hours"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Restroom and EV", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Search bar and filter buttons (pinned at the top)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search rest rooms, EV and hotels") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { searchFocused = it.isFocused }
                )

                if (searchFocused || filter != FacilityFilter.ALL) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        FacilityFilter.values().forEach { option ->
                            FilterChip(
                                selected = filter == option,
                                onClick = { filter = option },
                                label = { Text(option.label) }
                            )
                        }
                    }
                }
            }

            // Hero cards and the list
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    error.isNotEmpty() -> Text(error, Modifier.padding(16.dp))
                    else -> LazyColumn(
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    HeroCard(
                                        label = "Live location",
                                        title = "Nearby location",
                                        description = "Turn on live location to see the closest " +
                                                "rest rooms and EV stations on your route.",
                                        stat = "${all.size + hotels.size} stops on the " +
                                                "Mumbai-Pune route",
                                        container = MaterialTheme.colorScheme.primaryContainer,
                                        content = MaterialTheme.colorScheme.onPrimaryContainer,
                                        onClick = { showLocationInfo = true }
                                    )
                                }
                                item {
                                    HeroCard(
                                        label = "Stay",
                                        title = "Restroom",
                                        description = "Rest for a few hours before the next " +
                                                "stretch of your ride.",
                                        stat = restStat,
                                        container = MaterialTheme.colorScheme.secondaryContainer,
                                        content = MaterialTheme.colorScheme.onSecondaryContainer,
                                        onClick = { filter = FacilityFilter.RESTROOM }
                                    )
                                }
                                item {
                                    HeroCard(
                                        label = "Charge",
                                        title = "EV",
                                        description = "Park and charge your car while you eat " +
                                                "or rest.",
                                        stat = evStat,
                                        container = MaterialTheme.colorScheme.tertiaryContainer,
                                        content = MaterialTheme.colorScheme.onTertiaryContainer,
                                        onClick = { filter = FacilityFilter.EV }
                                    )
                                }
                                item {
                                    HeroCard(
                                        label = "Sleep",
                                        title = "Hotel + Room",
                                        description = "Pick a highway hotel that offers rooms " +
                                                "and sleep before the next stretch.",
                                        stat = hotelStat,
                                        container = MaterialTheme.colorScheme.surfaceVariant,
                                        content = MaterialTheme.colorScheme.onSurfaceVariant,
                                        onClick = { filter = FacilityFilter.HOTEL }
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                heading,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        if (total == 0) {
                            item {
                                Text(
                                    "Nothing found",
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(visibleFacilities, key = { "f_${it.id}" }) { facility ->
                                FacilityCard(
                                    item = facility,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            items(visibleHotels, key = { "h_${it.id}" }) { hotel ->
                                HotelRoomCard(
                                    mall = hotel,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLocationInfo) {
        AlertDialog(
            onDismissRequest = { showLocationInfo = false },
            title = { Text("Nearby location") },
            text = {
                Text(
                    "Live location is not connected yet. Once it is switched on, this card " +
                            "will show the rest rooms and EV stations closest to you."
                )
            },
            confirmButton = {
                TextButton(onClick = { showLocationInfo = false }) { Text("OK") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroCard(
    label: String,
    title: String,
    description: String,
    stat: String,
    container: Color,
    content: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
        modifier = Modifier
            .width(280.dp)
            .height(190.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
            }
            Text(
                stat,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
        }
    }
}

private fun priceUnitLabel(unit: String): String {
    val trimmed = unit.trim()
    val cleaned = if (trimmed.startsWith("per ", ignoreCase = true)) {
        trimmed.substring(4).trim()
    } else {
        trimmed
    }
    return if (cleaned.isEmpty()) "" else "/ $cleaned"
}

@Composable
private fun StatusPill(label: String, positive: Boolean) {
    val tone = if (positive) Color(0xFF16A34A) else Color(0xFFDC2626)
    Surface(
        shape = RoundedCornerShape(50),
        color = tone.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = tone,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun FacilityCard(
    item: Facility,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(item.address, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.width(8.dp))
                    StatusPill(
                        label = if (item.available) "Available" else "Unavailable",
                        positive = item.available
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(item.details)
                Text(
                    if (item.type == "restroom") {
                        "Sleeps up to ${item.capacity} person(s)"
                    } else {
                        "${item.capacity} charging port(s)"
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (item.available) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "₹${item.price.toInt()}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (item.available) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = priceUnitLabel(item.priceUnit),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.available) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun HotelRoomCard(
    mall: FoodMall,
    modifier: Modifier = Modifier
) {
    val hasPrice = mall.roomPrice > 0
    val open = mall.isOpen
    val capacity = if (mall.roomCapacity > 0) mall.roomCapacity else HOTEL_CAPACITY
    val amenities = mall.roomDetails.ifBlank { "Amenities not listed" }

    Card(modifier = modifier.fillMaxWidth()) {
        Column {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "HOTEL + ROOM",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            mall.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(mall.address, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.width(8.dp))
                    StatusPill(
                        label = if (open) "Open" else "Closed",
                        positive = open
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text("Rating: ${mall.rating}")
                Text(amenities)
                Text("Sleeps up to $capacity person(s)")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (hasPrice && open) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                if (hasPrice) {
                    val tone = if (open) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                    Text(
                        text = "₹${mall.roomPrice.toInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = tone
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "/ ${mall.roomPriceUnit.ifBlank { "12 hours" }}",
                        style = MaterialTheme.typography.titleMedium,
                        color = tone,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                } else {
                    Text(
                        text = "Room rates coming soon",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}