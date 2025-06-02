package com.example.blood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupDetailsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var bloodGroup: String
    private val db = FirebaseFirestore.getInstance()
    private var hospitalId: String? = null

    companion object {
        private const val ARG_BLOOD_GROUP = "bloodGroup"
        fun newInstance(bloodGroup: String): GroupDetailsBottomSheet {
            val fragment = GroupDetailsBottomSheet()
            val args = Bundle()
            args.putString(ARG_BLOOD_GROUP, bloodGroup)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bloodGroup = arguments?.getString(ARG_BLOOD_GROUP) ?: ""
        hospitalId = FirebaseAuth.getInstance().currentUser?.phoneNumber
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_group_details_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val headerText = view.findViewById<TextView>(R.id.bloodGroupHeader)
        val infoContainer = view.findViewById<LinearLayout>(R.id.infoContainer)
        headerText.text = "Details for $bloodGroup"
        fetchAndDisplayInfo(infoContainer)
    }

    private fun fetchAndDisplayInfo(container: LinearLayout) {
        // Priority 1: Notified Requesters
        db.collection("BloodRequests")
            .whereEqualTo("bloodGroup", bloodGroup)
            .whereEqualTo("status", "notified")
            .get().addOnSuccessListener { notified ->
                if (!notified.isEmpty) {
                    addSectionTitle("🔔 Notified Requesters", container)
                    notified.forEach { doc ->
                        val requester = doc.getString("requesterName") ?: "Unknown"
                        val phone = doc.getString("requester") ?: "Unknown"
                        addText("• $requester : $phone", container)
                    }
                }

                // Priority 2: Interested Donors
                var interestedAdded = false
                notified.forEach { doc ->
                    val donors = doc.get("interestedDonors") as? List<*> ?: emptyList<String>()
                    if (donors.isNotEmpty() && !interestedAdded) {
                        addSectionTitle("❤️ Interested Donors", container)
                        interestedAdded = true
                    }
                    donors.forEach { donor ->
                        addText("• $donor", container)
                    }
                }

                // Priority 3: Past Donors
                if (hospitalId != null) {
                    db.collection("hospitals")
                        .document(hospitalId!!)
                        .collection("inventory")
                        .document(bloodGroup)
                        .get()
                        .addOnSuccessListener { document ->
                            val donors = document.get("Donors") as? List<String>
                            if (!donors.isNullOrEmpty()) {
                                addSectionTitle("🩸 Other Donors (Past)", container)
                                donors.distinct().forEach { donor ->
                                    addText("• $donor", container)
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to load past donors", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun addSectionTitle(title: String, container: ViewGroup) {
        val titleView = TextView(requireContext()).apply {
            text = title
            textSize = 16f
            setTextColor(resources.getColor(R.color.primary_dark, null))
            setPadding(0, 20, 0, 10)
        }
        container.addView(titleView)
    }

    private fun addText(text: String, container: ViewGroup) {
        val textView = TextView(requireContext()).apply {
            this.text = text
            textSize = 15f
            setPadding(10, 4, 0, 4)
        }
        container.addView(textView)
    }
}
