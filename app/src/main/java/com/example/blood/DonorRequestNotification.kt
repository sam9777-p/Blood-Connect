package com.example.blood

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class DonorRequestNotification : AppCompatActivity() {

    private lateinit var bloodGroupText: TextView
    private lateinit var requesterText: TextView
    private lateinit var unitsText: TextView
    private lateinit var cityText: TextView
    private lateinit var btnDonate: Button
    private lateinit var btnIgnore: Button

    private var requestId: String? = null
    private var requesterName: String? = null
    private var bloodGroup: String? = null
    private var city: String? = null
    private var units: String? = null
    private lateinit var hospitalText: TextView
    private var handledBy: String? = null
    private lateinit var btnNavigate: Button
    private var hospitalName: String? = null
    private var hospitalId: String? = null
    private var hospitalLat: Double? = null
    private var hospitalLng: Double? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_request_notification)

        bloodGroupText = findViewById(R.id.bloodGroupText)
        requesterText = findViewById(R.id.requesterText)
        unitsText = findViewById(R.id.unitsText)
        cityText = findViewById(R.id.cityText)
        btnDonate = findViewById(R.id.btnDonate)
        btnIgnore = findViewById(R.id.btnIgnore)
        hospitalText = findViewById(R.id.hospitalText)
        // Get data from intent
        requestId = intent.getStringExtra("requestId")
        requesterName = intent.getStringExtra("requesterName")
        bloodGroup = intent.getStringExtra("bloodGroup")
        city = intent.getStringExtra("city")
        units = intent.getStringExtra("units")
        hospitalId  = intent.getStringExtra("handledBy")


        hospitalText.text = "Hospital: $hospitalId "
        // Display data
        bloodGroupText.text = "Blood Group: $bloodGroup"
        requesterText.text = "Requester: $requesterName"
        unitsText.text = "Units Needed: $units"
        cityText.text = "City: $city"
        btnNavigate = findViewById(R.id.btnNavigate)
        val db = FirebaseFirestore.getInstance()
        hospitalId?.let { id ->
            db.collection("Accounts").document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        hospitalName = document.getString("hospitalName")
                        hospitalLat = document.getDouble("latitude")
                        hospitalLng = document.getDouble("longitude")
                        hospitalText.text = "Hospital: $hospitalName"
                    } else {
                        Toast.makeText(this, "Hospital not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch hospital location", Toast.LENGTH_SHORT).show()
                }
        }

        findViewById<TextView>(R.id.hospitalText).text = "Hospital: $hospitalName"

        btnNavigate.setOnClickListener {
            if (hospitalLat != null && hospitalLng != null) {
                val uri = "geo:$hospitalLat,$hospitalLng?q=$hospitalLat,$hospitalLng($hospitalId)"
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri))
                intent.setPackage("com.google.android.apps.maps")
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Google Maps not available", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Location not available yet", Toast.LENGTH_SHORT).show()
            }
        }



        btnDonate.setOnClickListener {
            markAsDonating()
        }

        btnIgnore.setOnClickListener {
            finish()
        }
    }

    private fun markAsDonating() {
        val donorPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber
        if (donorPhone == null || requestId == null) {
            Toast.makeText(this, "Error. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("BloodRequests").document(requestId!!)
            .update("interestedDonors", FieldValue.arrayUnion(donorPhone))
            .addOnSuccessListener {
                Toast.makeText(this, "Thank you for volunteering! Hospital will contact you.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, DonorActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update request. Try again later.", Toast.LENGTH_SHORT).show()
            }
    }

}
