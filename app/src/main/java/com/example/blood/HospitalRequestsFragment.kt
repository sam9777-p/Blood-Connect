package com.example.blood

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blood.databinding.FragmentHospitalRequestsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HospitalRequestsFragment : Fragment() {

    private var _binding: FragmentHospitalRequestsBinding? = null
    private val binding get() = _binding!!
    private val requestList = mutableListOf<BloodRequest>()
    private lateinit var adapter: HospitalRequestsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHospitalRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = HospitalRequestsAdapter(requestList,
            onAccept = { request -> acceptRequest(request) },
            onReject = { request -> rejectRequest(request) }
        )

        binding.hospitalRequestsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.hospitalRequestsRecyclerView.adapter = adapter

        fetchRequests()
    }

    private fun fetchRequests() {
        val phone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("BloodRequests")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                requestList.clear()
                for (doc in snapshot.documents) {
                    val request = doc.toObject(BloodRequest::class.java)
                    request?.let { requestList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun acceptRequest(request: BloodRequest) {
        val db = FirebaseFirestore.getInstance()
        val hospitalPhone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return

        val inventoryRef = db.collection("hospitals")
            .document(hospitalPhone)
            .collection("inventory")
            .document(request.bloodGroup)

        inventoryRef.get().addOnSuccessListener { snapshot ->
            val availableUnits = snapshot.getLong("units") ?: 0L

            if (availableUnits < request.units) {
                // Not enough blood → notify donors
                db.collection("BloodRequests").document(request.id)
                    .update("status", "notified", "handledBy", hospitalPhone)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Notified donors (low stock)", Toast.LENGTH_SHORT).show()
                        fetchRequests()
                    }
            } else {
                // Optional: Deduct units and mark as accepted
                inventoryRef.update("units", availableUnits - request.units).addOnSuccessListener {
                    //Log.d(TAG, "acceptRequest: ")
                    Log.d("id", "Actual: ${request.id}")
                    db.collection("BloodRequests").document(request.id)
                        .update("status", "fulfilled", "handledBy", hospitalPhone)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Request fulfilled from stock", Toast.LENGTH_SHORT).show()
                            fetchRequests()
                        }
                }
            }
        }
    }


    private fun rejectRequest(request: BloodRequest) {
        FirebaseFirestore.getInstance().collection("BloodRequests")
            .document(request.id)
            .update("status", "rejected")
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Request rejected", Toast.LENGTH_SHORT).show()
                fetchRequests()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
