package com.example.blood.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HomeRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUserData(): Map<String, String?> {
        val phone = auth.currentUser?.phoneNumber ?: return emptyMap()
        val doc = firestore.collection("Accounts").document(phone).get().await()

        return mapOf(
            "bloodGroup" to doc.getString("bloodGroup"),
            "firstName" to doc.getString("firstName"),
            "userId" to phone
        )
    }
}
