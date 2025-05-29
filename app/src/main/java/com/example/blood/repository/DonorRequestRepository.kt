package com.example.blood.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class DonorRequestRepository {

    private val db = FirebaseFirestore.getInstance()

    fun fetchHospitalDetails(hospitalId: String): LiveData<HospitalData?> {
        val data = MutableLiveData<HospitalData?>()
        db.collection("Accounts").document(hospitalId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val hospitalName = document.getString("hospitalName")
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")
                    data.value = HospitalData(hospitalName, latitude, longitude)
                } else {
                    data.value = null
                }
            }.addOnFailureListener {
                data.value = null
            }
        return data
    }

    fun markAsDonating(requestId: String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        val donorPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber
        if (donorPhone == null) {
            result.value = false
            return result
        }

        db.collection("BloodRequests").document(requestId)
            .update("interestedDonors", FieldValue.arrayUnion(donorPhone))
            .addOnSuccessListener {
                result.value = true
            }
            .addOnFailureListener {
                result.value = false
            }

        return result
    }

    data class HospitalData(val name: String?, val lat: Double?, val lng: Double?)
}
