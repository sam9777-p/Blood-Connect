package com.example.blood

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blood.adapters.InventoryAdapter
import com.example.blood.data.InventoryItem
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class HospitalHomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var inventoryAdapter: InventoryAdapter
    private val inventoryList = mutableListOf<InventoryItem>()
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hospital_home, container, false)
        shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmerLayout)
        recyclerView = view.findViewById(R.id.inventoryRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        inventoryAdapter = InventoryAdapter(inventoryList)
        recyclerView.adapter = inventoryAdapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showShimmer()
        fetchInventory()
    }

    private fun showShimmer() {
        shimmerLayout.startShimmer()
        shimmerLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun hideShimmer() {
        shimmerLayout.stopShimmer()
        shimmerLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }


    private fun fetchInventory() {
        val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return
        val db = FirebaseFirestore.getInstance()
        val inventoryRef = db.collection("hospitals").document(userId).collection("inventory")

        inventoryRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                // No inventory exists, so initialize
                val bloodGroups = listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-")
                val batch = db.batch()

                bloodGroups.forEach { bg ->
                    val docRef = inventoryRef.document(bg)
                    val data = mapOf("units" to 0, "status" to "Low")
                    batch.set(docRef, data)
                }

                batch.commit().addOnSuccessListener {
                    fetchInventory() // Reload after initializing
                }
            } else {
                // Inventory exists, display it
                val bloodList = mutableListOf<InventoryItem>()
                for (doc in snapshot) {
                    val type = doc.id
                    val units = doc.getLong("units")?.toInt() ?: 0
                    val status = doc.getString("status") ?: "Low"
                    bloodList.add(InventoryItem(type, units, status))
                }
                recyclerView.adapter= InventoryAdapter(bloodList)

                hideShimmer()
            }
        }.addOnFailureListener {
            hideShimmer()
        }
    }


}
