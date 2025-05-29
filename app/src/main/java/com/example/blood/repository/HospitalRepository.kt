package com.example.blood.repository

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.widget.Toast
import androidx.annotation.RequiresPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.LocationServices

class HospitalRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun doesHospitalProfileExist(callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.phoneNumber ?: return callback(false)
        firestore.collection("Accounts").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val exists = document.exists() && document.getString("hospitalName") != null
                callback(exists)
            }
            .addOnFailureListener { callback(false) }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun updateUserLocation(context: Context, callback: (String) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(context)
                    val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val city = addressList?.firstOrNull()?.locality ?: "Unknown"

                    val userId = auth.currentUser?.phoneNumber ?: return@addOnSuccessListener
                    val data = mapOf(
                        "latitude" to location.latitude,
                        "longitude" to location.longitude,
                        "city" to city
                    )

                    firestore.collection("Accounts").document(userId)
                        .update(data)
                        .addOnSuccessListener { callback("Location updated") }
                        .addOnFailureListener { callback("Failed to update location") }
                } else {
                    callback("Could not fetch location")
                }
            }
            .addOnFailureListener {
                callback("Failed to retrieve location")
            }
    }
}
