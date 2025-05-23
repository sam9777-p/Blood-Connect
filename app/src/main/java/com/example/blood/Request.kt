package com.example.blood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Request : Fragment() {

    private lateinit var bloodGroupSpinner: Spinner
    private lateinit var unitsEditText: EditText
    private lateinit var purposeEditText: EditText
    private lateinit var requestButton: Button
    private val db = FirebaseFirestore.getInstance()

    private val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_request, container, false)

        bloodGroupSpinner = view.findViewById(R.id.bloodGroupSpinner)
        unitsEditText = view.findViewById(R.id.unitsEditText)
        purposeEditText = view.findViewById(R.id.purposeEditText)
        requestButton = view.findViewById(R.id.requestButton)

        bloodGroupSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, bloodGroups)

        requestButton.setOnClickListener {
            submitBloodRequest()
        }

        return view
    }

    private fun submitBloodRequest() {
        val selectedGroup = bloodGroupSpinner.selectedItem.toString()
        val units = unitsEditText.text.toString().toIntOrNull()
        val purpose = purposeEditText.text.toString().trim()
        val phone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return

        if (units == null || units <= 0 || purpose.isEmpty()) {
            Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        val userDoc = FirebaseFirestore.getInstance().collection("Accounts").document(phone)
        userDoc.get().addOnSuccessListener { snapshot ->
            val name = snapshot.getString("firstName") ?: ""
            val city = snapshot.getString("city") ?: ""
            val donorFcmToken = snapshot.getString("fcmToken") ?: ""
            if (name.isEmpty() || city.isEmpty()) {
                Toast.makeText(context, "Complete profile (name/city) required", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            val requestData = hashMapOf(
                "bloodGroup" to selectedGroup,
                "units" to units,
                "purpose" to purpose,
                "requester" to phone,
                "requesterName" to name,
                "city" to city,
                "timestamp" to Timestamp.now(),
                "status" to "pending",
                "fcm" to donorFcmToken
            )

            db.collection("BloodRequests")
                .add(requestData)
                .addOnSuccessListener { documentRef ->
                    db.collection("BloodRequests").document(documentRef.id)
                        .update("id", documentRef.id)
                    Toast.makeText(context, "Request submitted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to submit request", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
