package com.example.blood

import com.google.firebase.firestore.FirebaseFirestore

class RequestRepository {

    private val db = FirebaseFirestore.getInstance()

    fun submitRequest(
        request: BloodRequest,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("BloodRequests")
            .add(request)
            .addOnSuccessListener { doc ->
                db.collection("BloodRequests").document(doc.id).update("id", doc.id)
                onSuccess()
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getCurrentRequest(phone: String, onComplete: (BloodRequest?) -> Unit) {
        db.collection("BloodRequests")
            .whereEqualTo("requester", phone)
            .whereIn("status", listOf("pending", "notified"))
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                onComplete(snap.documents.firstOrNull()?.toObject(BloodRequest::class.java))
            }
    }
}
