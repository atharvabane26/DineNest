package com.dinenest.user.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Matches the Admin app's foodMalls document.
 * Room details and the 12-hour price are stored on the hotel document.
 */
data class FoodMall(
    @DocumentId val id: String = "",
    val name: String = "",
    val address: String = "",
    val rating: Double = 0.0,
    val hasRestRooms: Boolean = false,
    val roomPrice: Double = 0.0,
    val roomDetails: String = "",
    val roomPriceUnit: String = "12 hours",
    val roomCapacity: Int = 0,
    @get:PropertyName("isOpen") @set:PropertyName("isOpen")
    var isOpen: Boolean = true
)
