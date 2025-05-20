package com.example.blood

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Splash_Screen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val handler = android.os.Handler(Looper.getMainLooper())
        handler.postDelayed( {
            val user = FirebaseAuth.getInstance().currentUser
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
