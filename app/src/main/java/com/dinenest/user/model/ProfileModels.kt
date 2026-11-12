package com.dinenest.user.model

import com.google.firebase.firestore.DocumentId

data class UserProfile(
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val homeLocation: String = ""
)

data class SavedItem(
    @DocumentId val id: String = "",
    val type: String = "",
    val name: String = "",
    val address: String = ""
)