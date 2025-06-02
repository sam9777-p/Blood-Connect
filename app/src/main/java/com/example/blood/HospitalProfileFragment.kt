package com.example.blood

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.blood.viewmodel.HospitalProfileViewModel
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth

class HospitalProfileFragment : Fragment() {

    private lateinit var nameInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var contactInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var saveBtn: Button
    private lateinit var editBtn: Button
    private lateinit var cancelBtn: Button
    private lateinit var logoutBtn: Button

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    private lateinit var viewModel: HospitalProfileViewModel
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hospital_profile, container, false)
        logoutBtn = view.findViewById(R.id.logoutHospitalBtn)
        nameInput = view.findViewById(R.id.hospitalNameEditText)
        cityInput = view.findViewById(R.id.cityEditText)
        contactInput = view.findViewById(R.id.contactEditText)
        addressInput = view.findViewById(R.id.addressEditText)
        saveBtn = view.findViewById(R.id.saveHospitalProfileBtn)
        editBtn = view.findViewById(R.id.editHospitalProfileButton)
        cancelBtn = view.findViewById(R.id.cancelHospitalEditButton)

        viewModel = ViewModelProvider(this)[HospitalProfileViewModel::class.java]

        logoutBtn.setOnClickListener {
            showLogoutConfirmationDialog()
        }


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        requestLocation()

        editBtn.setOnClickListener { toggleEditMode(true) }
        cancelBtn.setOnClickListener {
            toggleEditMode(false)
            viewModel.loadProfile()
        }
        saveBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val city = cityInput.text.toString().trim()
            val contact = contactInput.text.toString().trim()
            val address = addressInput.text.toString().trim()

            if (name.isEmpty() || city.isEmpty() || contact.isEmpty() || address.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.saveProfile(name, city, contact, address, currentLocation)
            }
        }

        observeViewModel()
        toggleEditMode(false)
        viewModel.loadProfile()

        return view
    }

    private fun observeViewModel() {
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            nameInput.setText(data["hospitalName"] as? String ?: "")
            cityInput.setText(data["city"] as? String ?: "")
            contactInput.setText(data["contact"] as? String ?: "")
            addressInput.setText(data["address"] as? String ?: "")
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Hospital profile saved", Toast.LENGTH_SHORT).show()
                toggleEditMode(false)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
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

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
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

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), LoginSignupActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}
