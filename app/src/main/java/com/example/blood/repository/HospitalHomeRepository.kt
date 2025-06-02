package com.example.blood.repository

import androidx.lifecycle.MutableLiveData
import com.example.blood.data.InventoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HospitalHomeRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun fetchInventory(
        inventoryItems: MutableLiveData<List<InventoryItem>>,
        loading: MutableLiveData<Boolean>
    ) {
        val userId = auth.currentUser?.phoneNumber ?: return
        val inventoryRef = firestore.collection("hospitals").document(userId).collection("inventory")

        loading.value = true

        inventoryRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                initializeInventory(userId, inventoryItems, loading)
            } else {
                val list = snapshot.map { doc ->
                    val type = doc.id
                    val units = doc.getLong("units")?.toInt() ?: 0
                    val status = doc.getString("status") ?: "Low"
                    InventoryItem(type, units, status)
                }
                inventoryItems.value = list
                loading.value = false
            }
        }.addOnFailureListener {
            loading.value = false
        }
    }

    private fun initializeInventory(
        userId: String,
        inventoryItems: MutableLiveData<List<InventoryItem>>,
        loading: MutableLiveData<Boolean>
    ) {
        val bloodGroups = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")
        val inventoryRef = firestore.collection("hospitals").document(userId).collection("inventory")
        val batch = firestore.batch()

        for (group in bloodGroups) {
            val docRef = inventoryRef.document(group)
            val data = mapOf("units" to 0, "status" to "Low")
            batch.set(docRef, data)
        }

        batch.commit().addOnSuccessListener {
            fetchInventory(inventoryItems, loading)
        }.addOnFailureListener {
            loading.value = false
        }
    }
}
