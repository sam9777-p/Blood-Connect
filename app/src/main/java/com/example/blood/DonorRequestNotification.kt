package com.example.blood

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.blood.ui.donor.DonorActivity
import com.example.blood.viewmodel.DonorRequestViewModel

class DonorRequestNotification : AppCompatActivity() {

    private val viewModel: DonorRequestViewModel by viewModels()

    private lateinit var bloodGroupText: TextView
    private lateinit var requesterText: TextView
    private lateinit var unitsText: TextView
    private lateinit var cityText: TextView
    private lateinit var hospitalText: TextView
    private lateinit var btnDonate: Button
    private lateinit var btnIgnore: Button
    private lateinit var btnNavigate: Button

    private var requestId: String? = null
    private var requesterName: String? = null
    private var bloodGroup: String? = null
    private var city: String? = null
    private var units: String? = null
    private var hospitalId: String? = null
    private var hospitalLat: Double? = null
    private var hospitalLng: Double? = null
    private var hospitalName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_request_notification)

        // View Bindings
        bloodGroupText = findViewById(R.id.bloodGroupText)
        requesterText = findViewById(R.id.requesterText)
        unitsText = findViewById(R.id.unitsText)
        cityText = findViewById(R.id.cityText)
        hospitalText = findViewById(R.id.hospitalText)
        btnDonate = findViewById(R.id.btnDonate)
        btnIgnore = findViewById(R.id.btnIgnore)
        btnNavigate = findViewById(R.id.btnNavigate)

        // Intent Extras
        requestId = intent.getStringExtra("requestId")
        requesterName = intent.getStringExtra("requesterName")
        bloodGroup = intent.getStringExtra("bloodGroup")
        city = intent.getStringExtra("city")
        units = intent.getStringExtra("units")
        hospitalId = intent.getStringExtra("handledBy")

        // Set Initial Texts
        bloodGroupText.text = "Blood Group: $bloodGroup"
        requesterText.text = "Requester: $requesterName"
        unitsText.text = "Units Needed: $units"
        cityText.text = "City: $city"

        // Fetch hospital data
        hospitalId?.let { viewModel.loadHospitalDetails(it) }

        viewModel.hospitalData.observe(this) { hospital ->
            if (hospital != null) {
                hospitalName = hospital.name
                hospitalLat = hospital.lat
                hospitalLng = hospital.lng
                hospitalText.text = "Hospital: $hospitalName"
            } else {
                hospitalText.text = "Hospital: Not found"
            }
        }

        btnDonate.setOnClickListener {
            requestId?.let { viewModel.markAsDonating(it) }
        }

        viewModel.donationStatus.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Thank you for volunteering! Hospital will contact you.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, DonorActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Failed to update request. Try again later.", Toast.LENGTH_SHORT).show()
            }
        }

        btnIgnore.setOnClickListener {
            finish()
        }

        btnNavigate.setOnClickListener {
            if (hospitalLat != null && hospitalLng != null) {
                val uri = "geo:$hospitalLat,$hospitalLng?q=$hospitalLat,$hospitalLng($hospitalName)"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
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
    }
}
