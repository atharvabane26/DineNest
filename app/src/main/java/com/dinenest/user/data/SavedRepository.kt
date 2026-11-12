package com.dinenest.user.data

import com.dinenest.user.model.SavedItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object SavedRepository {
    private fun collection(): CollectionReference? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return FirebaseFirestore.getInstance()
            .collection("users").document(uid).collection("saved")
    }

    fun listen(onChange: (List<SavedItem>) -> Unit): ListenerRegistration? {
        val col = collection() ?: return null
        return col.addSnapshotListener { snapshot, _ ->
            onChange(snapshot?.toObjects(SavedItem::class.java) ?: emptyList())
        }
    }

    fun save(id: String, type: String, name: String, address: String) {
        collection()?.document(id)?.set(
            mapOf("type" to type, "name" to name, "address" to address)
        )
    }

    fun unsave(id: String) {
        collection()?.document(id)?.delete()
    }
}