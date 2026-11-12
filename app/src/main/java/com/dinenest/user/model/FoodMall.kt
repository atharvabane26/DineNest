package com.dinenest.user.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.DocumentId

data class FoodMall(
    @DocumentId val id: String = "",
    val name: String = "",
    val address: String = "",
    val rating: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val hasRestRooms: Boolean = false,
    val hasEvCharging: Boolean = false,
    @get:PropertyName("isOpen") @set:PropertyName("isOpen")
    var isOpen: Boolean = true
)