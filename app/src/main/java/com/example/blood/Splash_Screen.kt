package com.example.blood

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Splash_Screen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val handler = android.os.Handler(Looper.getMainLooper())
        handler.postDelayed( {
            val user = FirebaseAuth.getInstance().currentUser

            val extras = intent.extras

            // Check if it was opened via notification
            if (extras != null && extras.containsKey("type")) {
                when (extras.getString("type")) {
                    "request_fulfilled" -> {
                        val fulfilledIntent = Intent(this, RequestFulfilledActivity::class.java).apply {
                            putExtra("handledBy", extras.getString("handledBy"))
                            putExtra("bloodGroup", extras.getString("bloodGroup"))
                            putExtra("units", extras.getString("units")?.toIntOrNull() ?: 0)
                        }
                        startActivity(fulfilledIntent)
                        finish()
                        return@postDelayed
                    }

                    "blood_request" -> {
                        val requestIntent = Intent(this, DonorRequestNotification::class.java).apply {
                            putExtra("requestId", extras.getString("requestId"))
                            putExtra("bloodGroup", extras.getString("bloodGroup"))
                            putExtra("city", extras.getString("city"))
                            putExtra("requesterName", extras.getString("requesterName"))
                            putExtra("units", extras.getString("units"))
                            putExtra("handledBy", extras.getString("handledBy"))
                        }
                        startActivity(requestIntent)
                        finish()
                        return@postDelayed
                    }
                }
            }

            if (user != null) {
                // User already logged in
                val phoneNumber = user.phoneNumber
                val db = FirebaseFirestore.getInstance()
                db.collection("Accounts").document(phoneNumber.toString())
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val role = document.getString("role")
                            if (role == "Hospital") {
                                startActivity(Intent(this, HospitalActivity::class.java))
                            } else if (role == "Donor") {
                                startActivity(Intent(this, DonorActivity::class.java))
                            }
                            finish()
                        }
                        else{
                            startActivity(Intent(this, RoleSelectionActivity::class.java))
                            finish()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to check account", Toast.LENGTH_SHORT).show()
                    }
            } else {
                startActivity(Intent(this, LiquidSwipeOnboardingActivity::class.java)) // Or your phone input activity
                finish()
            }
        },3000)

    }
}
