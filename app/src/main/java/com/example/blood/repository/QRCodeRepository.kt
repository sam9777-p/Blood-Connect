package com.example.blood.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class QRCodeRepository {

    fun getUserData(callback: (String, String, String, String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("Accounts").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val bloodGroup = document.getString("bloodGroup") ?: "Unknown"
                val eligibility = document.getString("eligibility") ?: "Unknown"
                val date = document.getString("lastDonationDate") ?: "Unknown"
                callback(userId, bloodGroup, eligibility, date)
            }
        }
    }
}
