package com.example.blood.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class DonorRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getUserPhone() = auth.currentUser?.phoneNumber ?: ""

    fun fetchUserData(onComplete: (Map<String, String?>) -> Unit) {
        val phone = getUserPhone()
        db.collection("Accounts").document(phone).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val firstName = doc.getString("firstName")
                val phoneNumber = doc.getString("phonenumber") ?: phone
                val bloodGroup = doc.getString("bloodGroup") ?: "N/A"
                onComplete(mapOf(
                    "firstName" to firstName,
                    "phonenumber" to phoneNumber,
                    "bloodGroup" to bloodGroup
                ))

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        db.collection("Accounts").document(phone)
                            .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
                    }
                }
            }
        }
    }

    fun checkUserProfile(onComplete: (Boolean) -> Unit) {
        val phone = getUserPhone()
        db.collection("Accounts").document(phone).get()
            .addOnSuccessListener { doc ->
                onComplete(doc.exists() && doc.getString("eligibility") != null)
            }
    }

    fun logout() {
        auth.signOut()
    }
}
