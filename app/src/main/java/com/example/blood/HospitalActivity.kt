package com.example.blood

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView

class HospitalActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital)

        checkAndRequestLocationPermission()
        setupBottomNavigation()

        // Load proper fragment based on whether profile exists
        if (savedInstanceState == null) {
            val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber
            if (userId != null) {
                FirebaseFirestore.getInstance().collection("Accounts").document(userId)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists() && doc.getString("hospitalName") != null) {
                            // Load default home screen
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.hospitalFragmentContainer, HospitalHomeFragment())
                                .commit()
                        } else {
                            // No profile -> load profile fragment
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.hospitalFragmentContainer, HospitalProfileFragment())
                                .commit()
                        }
                    }
            }
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.hospitalTopAppBar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.hospitalFragmentContainer, HospitalProfileFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.hospitalBottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.hospital_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.hospitalFragmentContainer, HospitalHomeFragment())
                        .commit()
                    true
                }
                R.id.hospital_requests -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.hospitalFragmentContainer, HospitalRequestsFragment())
                        .commit()
                    true
                }
                R.id.hospital_scanner -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.hospitalFragmentContainer, HospitalQRFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }


    }

    private fun checkAndRequestLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            onLocationPermissionGranted()
        } else {
            val shouldShowRationale = missingPermissions.any {
                ActivityCompat.shouldShowRequestPermissionRationale(this, it)
            }

            if (shouldShowRationale) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app requires location access to manage nearby blood requests.")
                    .setPositiveButton("Allow") { _, _ ->
                        ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onLocationPermissionGranted()
            } else {
                Toast.makeText(this, "Location permission denied. Some features may not work.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onLocationPermissionGranted() {
        val fusedLocationProviderClient = com.google.android.gms.location.LocationServices
            .getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions are already checked before calling this, but return for safety
            return
        }

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    // Use Geocoder to convert lat/lng to a city name
                    val geocoder = android.location.Geocoder(this)
                    val addressList = geocoder.getFromLocation(latitude, longitude, 1)
                    val city = if (!addressList.isNullOrEmpty()) {
                        addressList[0].locality ?: "Unknown"
                    } else "Unknown"

                    val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber
                    if (userId != null) {
                        val data = mapOf(
                            "latitude" to latitude,
                            "longitude" to longitude,
                            "city" to city
                        )

                        FirebaseFirestore.getInstance()
                            .collection("Accounts")
                            .document(userId)
                            .update(data)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update location", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Could not fetch location", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
