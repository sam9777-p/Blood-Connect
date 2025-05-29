package com.example.blood

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.blood.viewmodel.ProfileViewModel
import com.google.android.gms.location.LocationServices
import java.util.*

class ProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel

    private lateinit var bloodGroupText: TextView
    private lateinit var genderGroup: RadioGroup
    private lateinit var ageInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var haemoglobinInput: EditText
    private lateinit var lastDonationDate: EditText
    private lateinit var saveButton: Button
    private lateinit var editButton: Button
    private lateinit var cancelButton: Button

    private lateinit var progressBar: ProgressBar


    private var isEditMode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        progressBar = view.findViewById(R.id.topProgressBar)

        bloodGroupText = view.findViewById(R.id.bloodGroupField)
        genderGroup = view.findViewById(R.id.genderGroup)
        ageInput = view.findViewById(R.id.ageInput)
        weightInput = view.findViewById(R.id.weightInput)
        heightInput = view.findViewById(R.id.heightInput)
        haemoglobinInput = view.findViewById(R.id.haemoglobinInput)
        lastDonationDate = view.findViewById(R.id.lastDonationDate)
        saveButton = view.findViewById(R.id.saveProfileButton)
        editButton = view.findViewById(R.id.editProfileButton)
        cancelButton = view.findViewById(R.id.cancelEditButton)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(fusedLocationClient, requireContext()) as T
            }
        })[ProfileViewModel::class.java]

        lastDonationDate.setOnClickListener { showDatePicker() }
        bloodGroupText.setOnClickListener { if (isEditMode) showBloodGroupDialog() }
        saveButton.setOnClickListener { performSave() }
        editButton.setOnClickListener { toggleEditMode(true) }
        cancelButton.setOnClickListener {
            viewModel.loadProfile()
            toggleEditMode(false)
        }

        observeViewModel()
        toggleEditMode(false)
        viewModel.loadProfile()
        return view
    }

    private fun observeViewModel() {
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            bloodGroupText.text = data["bloodGroup"] ?: ""
            ageInput.setText(data["age"] ?: "")
            weightInput.setText(data["weight"] ?: "")
            heightInput.setText(data["height"] ?: "")
            haemoglobinInput.setText(data["haemoglobin"] ?: "")
            lastDonationDate.setText(data["lastDonationDate"] ?: "")
            when (data["gender"]) {
                "Male" -> genderGroup.check(R.id.male)
                "Female" -> genderGroup.check(R.id.female)
                else -> genderGroup.check(R.id.other)
            }
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            hideProgress()
            Toast.makeText(requireContext(), if (success) "Profile & eligibility saved" else "Saved without eligibility", Toast.LENGTH_SHORT).show()
            if (success) toggleEditMode(false)
            viewModel.loadProfile()
        }
    }

    private fun performSave() {
        val genderId = genderGroup.checkedRadioButtonId
        val gender = view?.findViewById<RadioButton>(genderId)?.text?.toString() ?: ""
        val bloodGroup = bloodGroupText.text.toString()
        val ageStr = ageInput.text.toString()
        val weightStr = weightInput.text.toString()
        val heightStr = heightInput.text.toString()
        val haemoglobinStr = haemoglobinInput.text.toString()
        val donationDate = lastDonationDate.text.toString()

        if (bloodGroup.isBlank() || gender.isBlank() || ageStr.isBlank() ||
            weightStr.isBlank() || heightStr.isBlank() || haemoglobinStr.isBlank() || donationDate.isBlank()
        ) {
            Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        showProgress()
        viewModel.saveUserProfile(bloodGroup, gender, ageStr, weightStr, heightStr, haemoglobinStr, donationDate)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day -> lastDonationDate.setText("$day/${month + 1}/$year") },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun showBloodGroupDialog() {
        val groups = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Blood Group")
        builder.setItems(groups) { _, which ->
            bloodGroupText.text = groups[which]
        }
        builder.show()
    }

    private fun toggleEditMode(enable: Boolean) {
        isEditMode = enable
        bloodGroupText.isEnabled = enable
        for (i in 0 until genderGroup.childCount) {
            genderGroup.getChildAt(i).isEnabled = enable
        }
        ageInput.isEnabled = enable
        weightInput.isEnabled = enable
        heightInput.isEnabled = enable
        haemoglobinInput.isEnabled = enable
        lastDonationDate.isEnabled = enable

        saveButton.visibility = if (enable) View.VISIBLE else View.GONE
        cancelButton.visibility = if (enable) View.VISIBLE else View.GONE
        editButton.visibility = if (enable) View.GONE else View.VISIBLE
    }

    private fun showProgress() {
        progressBar.visibility = View.VISIBLE
        setInputsEnabled(false)
    }

    private fun hideProgress() {
        progressBar.visibility = View.GONE
        setInputsEnabled(true)
    }

    private fun setInputsEnabled(enabled: Boolean) {
        bloodGroupText.isEnabled = enabled
        genderGroup.isEnabled = enabled
        ageInput.isEnabled = enabled
        weightInput.isEnabled = enabled
        heightInput.isEnabled = enabled
        haemoglobinInput.isEnabled = enabled
        lastDonationDate.isEnabled = enabled
        saveButton.isEnabled = enabled
        editButton.isEnabled = enabled
        cancelButton.isEnabled = enabled
    }

}
