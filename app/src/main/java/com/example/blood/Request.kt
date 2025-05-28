package com.example.blood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Request : Fragment() {

    private lateinit var bloodGroupSpinner: Spinner
    private lateinit var unitsEditText: EditText
    private lateinit var purposeEditText: EditText
    private lateinit var requestButton: Button
    private lateinit var submittedSection: LinearLayout
    private lateinit var requestDetails: TextView
    private lateinit var newRequestButton: Button
    private val db = FirebaseFirestore.getInstance()

    private val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_request, container, false)

        bloodGroupSpinner = view.findViewById(R.id.bloodGroupSpinner)
        unitsEditText = view.findViewById(R.id.unitsEditText)
        purposeEditText = view.findViewById(R.id.purposeEditText)
        requestButton = view.findViewById(R.id.requestButton)
        submittedSection = view.findViewById(R.id.submittedSection)
        requestDetails = view.findViewById(R.id.requestDetails)
        newRequestButton = view.findViewById(R.id.newRequestButton)

        bloodGroupSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, bloodGroups)

        requestButton.setOnClickListener { submitBloodRequest() }
        newRequestButton.setOnClickListener { toggleFormVisibility(true) }

        checkForExistingRequest()

        return view
    }

    private fun checkForExistingRequest() {
        val phone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return

        db.collection("BloodRequests")
            .whereEqualTo("requester", phone)
            .whereIn("status", listOf("pending", "accepted"))
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents.first()
                    showSubmittedRequest(doc.data)
                } else {
                    toggleFormVisibility(true)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to check request", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSubmittedRequest(data: Map<String, Any>?) {
        toggleFormVisibility(false)
        data?.let {
            val group = it["bloodGroup"] ?: ""
            val units = it["units"] ?: ""
            val purpose = it["purpose"] ?: ""
            val status = it["status"] ?: ""
            val handledBy = it["handledBy"] as? String
            val donors = it["interestedDonors"] as? List<*> ?: emptyList<String>()

            val baseDetails = "Blood Group: $group\nUnits: $units\nPurpose: $purpose\nStatus: ${status.toString().capitalize()}"
            requestDetails.text = baseDetails

            // Show handledBy if present
            val handledByText = view?.findViewById<TextView>(R.id.handledByText)
            handledByText?.visibility = if (!handledBy.isNullOrEmpty()) View.VISIBLE else View.GONE
            handledByText?.text = "Handled By: $handledBy"

            // Show interested donors
            val donorListHeader = view?.findViewById<TextView>(R.id.donorListHeader)
            val donorListLayout = view?.findViewById<LinearLayout>(R.id.donorListLayout)

            if (donors.isNotEmpty()) {
                donorListHeader?.visibility = View.VISIBLE
                donorListLayout?.visibility = View.VISIBLE
                donorListLayout?.removeAllViews()

                donors.forEach { donor ->
                    val donorText = TextView(requireContext()).apply {
                        text = "• $donor"
                        setTextColor(resources.getColor(android.R.color.black))
                        textSize = 15f
                        setPadding(0, 4, 0, 4)
                    }
                    donorListLayout?.addView(donorText)
                }
            } else {
                donorListHeader?.visibility = View.GONE
                donorListLayout?.visibility = View.GONE
            }
        }
    }


    private fun toggleFormVisibility(showForm: Boolean) {
        val formVisible = showForm
        bloodGroupSpinner.isVisible = formVisible
        unitsEditText.isVisible = formVisible
        purposeEditText.isVisible = formVisible
        requestButton.isVisible = formVisible

        submittedSection.isVisible = !formVisible
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

        val userDoc = db.collection("Accounts").document(phone)
        userDoc.get().addOnSuccessListener { snapshot ->
            val name = snapshot.getString("firstName") ?: ""
            val city = snapshot.getString("city") ?: ""
            val fcm = snapshot.getString("fcmToken") ?: ""

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
                "fcm" to fcm,
                "volunteers" to emptyList<String>() // Initially empty
            )

            db.collection("BloodRequests")
                .add(requestData)
                .addOnSuccessListener { docRef ->
                    db.collection("BloodRequests").document(docRef.id).update("id", docRef.id)
                    Toast.makeText(context, "Request submitted", Toast.LENGTH_SHORT).show()
                    checkForExistingRequest()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to submit request", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
