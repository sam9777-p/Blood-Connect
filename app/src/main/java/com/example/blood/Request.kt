package com.example.blood

import android.os.Bundle
import android.view.*
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
    private lateinit var cancelRequestButton: Button
    private lateinit var handledByText: TextView
    private lateinit var donorListHeader: TextView
    private lateinit var newRequestLayout: LinearLayout
    private lateinit var donorListLayout: LinearLayout

    private val db = FirebaseFirestore.getInstance()

    private val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_request, container, false)
        newRequestLayout = view.findViewById(R.id.newRequestlayout)
        bloodGroupSpinner = view.findViewById(R.id.bloodGroupSpinner)
        unitsEditText = view.findViewById(R.id.unitsEditText)
        purposeEditText = view.findViewById(R.id.purposeEditText)
        requestButton = view.findViewById(R.id.requestButton)
        submittedSection = view.findViewById(R.id.submittedSection)
        requestDetails = view.findViewById(R.id.requestDetails)
        newRequestButton = view.findViewById(R.id.newRequestButton)
        cancelRequestButton = view.findViewById(R.id.cancelRequestButton)
        handledByText = view.findViewById(R.id.handledByText)
        donorListHeader = view.findViewById(R.id.donorListHeader)
        donorListLayout = view.findViewById(R.id.donorListLayout)

        bloodGroupSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, bloodGroups)

        requestButton.setOnClickListener { submitBloodRequest() }
        newRequestButton.setOnClickListener { toggleFormVisibility(true) }
        cancelRequestButton.setOnClickListener { deleteCurrentRequest() }

        checkForExistingRequest()

        return view
    }

    private fun checkForExistingRequest() {
        val phone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return

        db.collection("BloodRequests")
            .whereEqualTo("requester", phone)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents.first()
                    val data = doc.data
                    val status = data?.get("status").toString().lowercase()
                    if (status == "pending" || status == "notified") {
                        showSubmittedRequest(data, doc.id)
                    } else if (status == "fulfilled") {
                        showSubmittedRequest(data, doc.id, allowNewRequest = true)
                    } else {
                        toggleFormVisibility(true)
                    }
                } else {
                    toggleFormVisibility(true)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to check request", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSubmittedRequest(data: Map<String, Any>?, requestId: String, allowNewRequest: Boolean = false) {
        toggleFormVisibility(false)

        data?.let {
            val group = it["bloodGroup"] ?: ""
            val units = it["units"] ?: ""
            val purpose = it["purpose"] ?: ""
            val status = it["status"]?.toString()?.capitalize() ?: ""
            val handledBy = it["handledBy"] as? String
            val donors = it["interestedDonors"] as? List<*> ?: emptyList<String>()

            val details = "Blood Group: $group\nUnits: $units\nPurpose: $purpose\nStatus: $status"
            requestDetails.text = details

            handledByText.visibility = if (!handledBy.isNullOrEmpty()) View.VISIBLE else View.GONE
            handledByText.text = "Handled By: $handledBy"

            if (donors.isNotEmpty()) {
                donorListHeader.visibility = View.VISIBLE
                donorListLayout.visibility = View.VISIBLE
                donorListLayout.removeAllViews()
                donors.forEach { donor ->
                    val donorText = TextView(requireContext()).apply {
                        text = "• $donor"
                        setTextColor(resources.getColor(android.R.color.black))
                        textSize = 15f
                        setPadding(0, 4, 0, 4)
                    }
                    donorListLayout.addView(donorText)
                }
            } else {
                donorListHeader.visibility = View.GONE
                donorListLayout.visibility = View.GONE
            }

            cancelRequestButton.visibility = if (!allowNewRequest) View.VISIBLE else View.GONE
            newRequestButton.visibility = if (allowNewRequest) View.VISIBLE else View.GONE
        }
    }

    private fun deleteCurrentRequest() {
        val phone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return
        db.collection("BloodRequests")
            .whereEqualTo("requester", phone)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val docId = result.documents.first().id
                    db.collection("BloodRequests").document(docId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Request cancelled", Toast.LENGTH_SHORT).show()
                            toggleFormVisibility(true)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to cancel request", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun toggleFormVisibility(showForm: Boolean) {
        newRequestLayout.isVisible = showForm
        submittedSection.isVisible = !showForm

        cancelRequestButton.isVisible = false
        newRequestButton.isVisible = false
        handledByText.visibility = View.GONE
        donorListHeader.visibility = View.GONE
        donorListLayout.visibility = View.GONE
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

        db.collection("Accounts").document(phone).get().addOnSuccessListener { snapshot ->
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
                "volunteers" to emptyList<String>(),
                "interestedDonors" to emptyList<String>()
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
