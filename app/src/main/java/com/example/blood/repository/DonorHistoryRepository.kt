package com.example.blood.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DonorHistoryRepository {

    fun getDonationHistory(
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val phone = FirebaseAuth.getInstance().currentUser?.phoneNumber
        if (phone == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        FirebaseFirestore.getInstance().collection("Accounts")
            .document(phone)
            .get()
            .addOnSuccessListener { doc ->
                val history = doc.get("donationHistory") as? List<String>
                onSuccess(history ?: emptyList())
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}
