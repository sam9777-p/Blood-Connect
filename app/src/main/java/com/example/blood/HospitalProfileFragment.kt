package com.example.blood

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.android.gms.location.*

class HospitalProfileFragment : Fragment() {

    private lateinit var nameInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var contactInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var saveBtn: Button
    private lateinit var editBtn: Button
    private lateinit var cancelBtn: Button

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hospital_profile, container, false)

        nameInput = view.findViewById(R.id.hospitalNameEditText)
        cityInput = view.findViewById(R.id.cityEditText)
        contactInput = view.findViewById(R.id.contactEditText)
        addressInput = view.findViewById(R.id.addressEditText)
        saveBtn = view.findViewById(R.id.saveHospitalProfileBtn)
        editBtn = view.findViewById(R.id.editHospitalProfileButton)
        cancelBtn = view.findViewById(R.id.cancelHospitalEditButton)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        requestLocation()

        editBtn.setOnClickListener { toggleEditMode(true) }
        cancelBtn.setOnClickListener {
            toggleEditMode(false)
            loadHospitalProfile()
        }
        saveBtn.setOnClickListener { saveHospitalProfile() }

        toggleEditMode(false)
        loadHospitalProfile()

        return view
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 1001)
        } else {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                }
            }
        }
    }

    private fun loadHospitalProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("Accounts").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                nameInput.setText(doc.getString("hospitalName") ?: "")
                cityInput.setText(doc.getString("city") ?: "")
                contactInput.setText(doc.getString("contact") ?: "")
                addressInput.setText(doc.getString("address") ?: "")
            }
        }
    }

    private fun toggleEditMode(enabled: Boolean) {
        isEditMode = enabled

        nameInput.isEnabled = enabled
        cityInput.isEnabled = enabled
        contactInput.isEnabled = enabled
        addressInput.isEnabled = enabled

        saveBtn.visibility = if (enabled) View.VISIBLE else View.GONE
        cancelBtn.visibility = if (enabled) View.VISIBLE else View.GONE
        editBtn.visibility = if (!enabled) View.VISIBLE else View.GONE
    }

    private fun saveHospitalProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return

        val name = nameInput.text.toString().trim()
        val city = cityInput.text.toString().trim()
        val contact = contactInput.text.toString().trim()
        val address = addressInput.text.toString().trim()

        if (name.isEmpty() || city.isEmpty() || contact.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val locationData = currentLocation?.let {
            mapOf("latitude" to it.latitude, "longitude" to it.longitude)
        } ?: emptyMap()

        val profileData = mapOf(
            "hospitalName" to name,
            "city" to city,
            "contact" to contact,
            "address" to address,
            "role" to "Hospital"
        ) + locationData

        FirebaseFirestore.getInstance()
            .collection("Accounts")
            .document(userId)
            .set(profileData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Hospital profile saved", Toast.LENGTH_SHORT).show()
                toggleEditMode(false)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save profile", Toast.LENGTH_SHORT).show()
            }
    }
}
