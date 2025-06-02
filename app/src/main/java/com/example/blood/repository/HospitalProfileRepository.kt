package com.example.blood.repository

import android.location.Location
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class HospitalProfileRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.phoneNumber
    }

    fun loadProfile(
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getCurrentUserId() ?: return
        firestore.collection("Accounts").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    onSuccess(doc.data ?: emptyMap())
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun saveProfile(
        name: String,
        city: String,
        contact: String,
        address: String,
        location: Location?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getCurrentUserId() ?: return

        val locationData = location?.let {
            mapOf("latitude" to it.latitude, "longitude" to it.longitude)
        } ?: emptyMap()

        val profileData = mapOf(
            "hospitalName" to name,
            "city" to city,
            "contact" to contact,
            "address" to address,
            "role" to "Hospital"
        ) + locationData

        firestore.collection("Accounts").document(userId)
            .set(profileData, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
}
