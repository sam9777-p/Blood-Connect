package com.example.blood.repository

import com.example.blood.data.HospitalsData
import com.google.firebase.firestore.FirebaseFirestore

class NearbyHospitalsRepository {

    private val db = FirebaseFirestore.getInstance()

    fun updateDonorCity(phone: String, city: String) {
        db.collection("Accounts").document(phone).update("city", city)
    }

    fun loadHospitalsByCity(city: String, callback: (List<HospitalsData>) -> Unit) {
        db.collection("Accounts")
            .whereEqualTo("role", "Hospital")
            .whereEqualTo("city", city)
            .get()
            .addOnSuccessListener { hospitalDocs ->
                val hospitalList = mutableListOf<HospitalsData>()
                for (doc in hospitalDocs) {
                    val hospital = HospitalsData(
                        phoneNumber = doc.id,
                        hospitalName = doc.getString("hospitalName") ?: "",
                        address = doc.getString("address") ?: "",
                        city = city,
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0
                    )
                    hospitalList.add(hospital)
                }
                callback(hospitalList)
            }
    }
}
