package com.example.blood

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit


class ProfileFragment : Fragment() {

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

    private lateinit var selectedBloodGroup: String
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

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

        lastDonationDate.setOnClickListener { showDatePicker() }

        saveButton.setOnClickListener { saveUserProfile() }

        editButton.setOnClickListener { toggleEditMode(true) }

        cancelButton.setOnClickListener {
            loadProfileData()
            toggleEditMode(false)
        }

        bloodGroupText.setOnClickListener {
            if (isEditMode) showBloodGroupDialog()
        }

        toggleEditMode(false)
        loadProfileData()
        return view
    }

    private fun toggleEditMode(enabled: Boolean) {
        isEditMode = enabled

        ageInput.isEnabled = enabled
        weightInput.isEnabled = enabled
        heightInput.isEnabled = enabled
        haemoglobinInput.isEnabled = enabled
        lastDonationDate.isEnabled = enabled
        for (i in 0 until genderGroup.childCount) {
            genderGroup.getChildAt(i).isEnabled = enabled
        }

        saveButton.visibility = if (enabled) View.VISIBLE else View.GONE
        cancelButton.visibility = if (enabled) View.VISIBLE else View.GONE
        editButton.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    private fun loadProfileData() {
        val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return

        val db = FirebaseFirestore.getInstance()

        db.collection("Accounts").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                bloodGroupText.text = document.getString("bloodGroup") ?: ""
                ageInput.setText(document.getString("age") ?: "")
                weightInput.setText(document.getString("weight") ?: "")
                heightInput.setText(document.getString("height") ?: "")
                haemoglobinInput.setText(document.getString("haemoglobin") ?: "")
                lastDonationDate.setText(document.getString("lastDonationDate") ?: "")

                val gender = document.getString("gender") ?: ""
                when (gender) {
                    "Male" -> genderGroup.check(R.id.male)
                    "Female" -> genderGroup.check(R.id.female)
                    else -> genderGroup.check(R.id.other)
                }
                toggleEditMode(false)
            }
            else{
                toggleEditMode(true)
            }

        }
    }

    private fun saveUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return
        val db = FirebaseFirestore.getInstance()

        val genderId = genderGroup.checkedRadioButtonId
        val gender = view?.findViewById<RadioButton>(genderId)?.text?.toString() ?: ""

        val bloodGroup = bloodGroupText.text.toString()
        val ageStr = ageInput.text.toString()
        val weightStr = weightInput.text.toString()
        val heightStr = heightInput.text.toString()
        val haemoglobinStr = haemoglobinInput.text.toString()
        val donationDate = lastDonationDate.text.toString()

        // Basic validation
        if (bloodGroup.isEmpty() || gender.isEmpty() || ageStr.isEmpty() || weightStr.isEmpty() ||
            heightStr.isEmpty() || haemoglobinStr.isEmpty() || donationDate.isEmpty()
        ) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageStr.toIntOrNull()
        val weight = weightStr.toIntOrNull()
        val height = heightStr.toFloatOrNull()
        val haemoglobin = haemoglobinStr.toFloatOrNull()

        if (age == null || weight == null || height == null || haemoglobin == null) {
            Toast.makeText(requireContext(), "Invalid numeric input", Toast.LENGTH_SHORT).show()
            return
        }

        val userMap = hashMapOf<String, Any>(
            "bloodGroup" to bloodGroup,
            "gender" to gender,
            "age" to ageStr,
            "weight" to weightStr,
            "height" to heightStr,
            "haemoglobin" to haemoglobinStr,
            "lastDonationDate" to donationDate
        )

        // Save basic data first
        db.collection("Accounts").document(userId)
            .set(userMap, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "Basic profile saved")
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to save profile", it)
            }

        // Call eligibility API
        /*
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // Increase connection timeout
            .readTimeout(60, TimeUnit.SECONDS) // Increase read timeout
            .writeTimeout(60, TimeUnit.SECONDS) // Increase write timeout
            .build()*/

        val retrofit = Retrofit.Builder()
            .baseUrl("https://blood-ha-api.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(EligibilityApi::class.java)

        val request = EligibilityRequest(
            height = height,
            weight = weight,
            gender = gender,
            age = age,
            hemoglobin = haemoglobin
        )
        Log.d("ProfileData", "Sending to API: $request")

        api.analyzeEligibility(request).enqueue(object : Callback<EligibilityResponse> {
            override fun onResponse(call: Call<EligibilityResponse>, response: Response<EligibilityResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val eligibility = response.body()!!.eligibility
                    db.collection("Accounts").document(userId)
                        .set(mapOf("eligibility" to eligibility), SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Profile & eligibility saved", Toast.LENGTH_SHORT).show()
                            toggleEditMode(false)
                            loadProfileData()
                        }
                } else {
                    Log.e("API2", "Response error: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "Saved without eligibility", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<EligibilityResponse>, t: Throwable) {
                Log.e("API", "Failure: ${t.message}")
                Toast.makeText(requireContext(), "Saved without eligibility", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                lastDonationDate.setText("$dayOfMonth/${month + 1}/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showBloodGroupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_blood_group, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val ids = listOf(
            R.id.bgAPlus, R.id.bgAMinus, R.id.bgBPlus, R.id.bgBMinus,
            R.id.bgABPlus, R.id.bgABMinus, R.id.bgOPlus, R.id.bgOMinus
        )
        val values = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

        for (i in ids.indices) {
            dialogView.findViewById<Button>(ids[i]).setOnClickListener {
                selectedBloodGroup = values[i]
                bloodGroupText.text = values[i]
                dialog.dismiss()
            }
        }

        dialogView.findViewById<Button>(R.id.centerCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
