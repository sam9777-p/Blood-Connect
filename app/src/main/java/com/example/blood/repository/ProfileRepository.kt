package com.example.blood.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.blood.EligibilityApi
import com.example.blood.data.EligibilityRequest
import com.example.blood.data.EligibilityResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class ProfileRepository(private val fusedLocationClient: FusedLocationProviderClient, private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String = auth.currentUser?.phoneNumber ?: ""

    fun loadProfile(onComplete: (Map<String, String?>) -> Unit) {
        db.collection("Accounts").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val data = mapOf(
                    "bloodGroup" to document.getString("bloodGroup"),
                    "age" to document.getString("age"),
                    "weight" to document.getString("weight"),
                    "height" to document.getString("height"),
                    "haemoglobin" to document.getString("haemoglobin"),
                    "lastDonationDate" to document.getString("lastDonationDate"),
                    "gender" to document.getString("gender")
                )
                onComplete(data)
            } else {
                onComplete(emptyMap())
            }
        }
    }

    fun saveUserProfile(
        bloodGroup: String,
        gender: String,
        ageStr: String,
        weightStr: String,
        heightStr: String,
        haemoglobinStr: String,
        donationDate: String,
        onFinished: (Boolean) -> Unit
    ) {
        val userMap = hashMapOf<String, Any>(
            "bloodGroup" to bloodGroup,
            "gender" to gender,
            "age" to ageStr,
            "weight" to weightStr,
            "height" to heightStr,
            "haemoglobin" to haemoglobinStr,
            "lastDonationDate" to donationDate
        )
        db.collection("Accounts").document(userId)
            .set(userMap, SetOptions.merge())
            .addOnSuccessListener { Log.d("Firestore", "Profile saved") }
            .addOnFailureListener { Log.e("Firestore", "Profile save failed", it) }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val city = addresses[0].locality
                        city?.let {
                            db.collection("Accounts").document(userId)
                                .set(mapOf("city" to it), SetOptions.merge())
                        }
                    }
                }
            }
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            db.collection("Accounts").document(userId)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://blood-ha-api.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(EligibilityApi::class.java)
        val request = EligibilityRequest(
            height = heightStr.toFloat(),
            weight = weightStr.toInt(),
            gender = gender,
            age = ageStr.toInt(),
            hemoglobin = haemoglobinStr.toFloat()
        )
        api.analyzeEligibility(request).enqueue(object : Callback<EligibilityResponse> {
            override fun onResponse(call: Call<EligibilityResponse>, response: Response<EligibilityResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val eligibility = response.body()!!.eligibility
                    db.collection("Accounts").document(userId)
                        .set(mapOf("eligibility" to eligibility), SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d("Firestore", "Eligibility saved")
                            onFinished(true)
                        }
                        .addOnFailureListener {
                            Log.e("Firestore", "Eligibility save failed", it)
                            onFinished(false)
                        }
                } else {
                    Log.e("API", "Error response: ${response.code()}")
                    onFinished(false)
                }
            }

            override fun onFailure(call: Call<EligibilityResponse>, t: Throwable) {
                Log.e("API", "Failure: ${t.message}")
                onFinished(false)
            }
        })
    }
}
