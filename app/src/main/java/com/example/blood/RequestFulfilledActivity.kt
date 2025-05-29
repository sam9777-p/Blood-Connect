package com.example.blood

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.blood.ui.donor.DonorActivity
import com.google.firebase.firestore.FirebaseFirestore

class RequestFulfilledActivity : AppCompatActivity() {

    private lateinit var hospitalLatLng: Pair<Double, Double>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_fulfilled)

        val hospitalPhone = intent.getStringExtra("handledBy") ?: return
        val bloodGroup = intent.getStringExtra("bloodGroup") ?: ""
        val units = intent.getIntExtra("units", 0)

        val btnBack = findViewById<Button>(R.id.btnBackHome)
        val btnNavigate = findViewById<Button>(R.id.btnNavigate)
        val hospitalNameView = findViewById<TextView>(R.id.hospitalName)
        val hospitalAddressView = findViewById<TextView>(R.id.hospitalAddress)
        val contactView = findViewById<TextView>(R.id.hospitalContact)
        val requestInfoView = findViewById<TextView>(R.id.requestInfo)

        FirebaseFirestore.getInstance()
            .collection("Accounts")
            .document(hospitalPhone)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("hospitalName") ?: "Hospital"
                val address = doc.getString("address") ?: "No address available"
                val contact = doc.getString("contact") ?: "Not available"
                val lat = doc.getDouble("latitude")
                val lng = doc.getDouble("longitude")

                hospitalNameView.text = name
                hospitalAddressView.text = "Address: $address"
                contactView.text = "Contact: $contact"
                requestInfoView.text = "Your request for $units unit(s) of $bloodGroup has been fulfilled."

                if (lat != null && lng != null) {
                    hospitalLatLng = lat to lng
                    btnNavigate.setOnClickListener {
                        val gmmIntentUri = Uri.parse("geo:${lat},${lng}?q=${Uri.encode(name)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        startActivity(mapIntent)
                    }
                    btnNavigate.isEnabled = true
                } else {
                    btnNavigate.isEnabled = false
                    btnNavigate.text = "Location not available"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load hospital data", Toast.LENGTH_SHORT).show()
            }

        btnBack.setOnClickListener {
            startActivity(Intent(this, DonorActivity::class.java))
            finish()
        }
    }
}
