package com.dinenest.user.model

import com.google.firebase.firestore.DocumentId

data class Facility(
    @DocumentId val id: String = "",
    val name: String = "",
    val type: String = "",
    val address: String = "",
    val details: String = "",
    val price: Double = 0.0,
    val priceUnit: String = "",
    val capacity: Int = 0,
    val available: Boolean = true
)