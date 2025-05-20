package com.example.blood

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class DonorHistory : Fragment(R.layout.fragment_donor_history) {

    private lateinit var recycler: RecyclerView
    private lateinit var myAdapter: HistoryAdapter
    private lateinit var noHistoryText: TextView
    private val historyList = mutableListOf<String>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler=view.findViewById(R.id.donationRecyclerView)
        noHistoryText=view.findViewById(R.id.noHistoryText)
        myAdapter = HistoryAdapter(historyList)
        recycler.adapter = myAdapter
        recycler.layoutManager = LinearLayoutManager(requireContext())
        fetchDonationHistory()
    }

    private fun fetchDonationHistory() {
        val phone = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return
        FirebaseFirestore.getInstance().collection("Accounts")
            .document(phone)
            .get()
            .addOnSuccessListener { doc ->
                val history = doc.get("donationHistory") as? List<String>
                if (history.isNullOrEmpty()) {
                    noHistoryText.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                } else {
                    historyList.clear()
                    historyList.addAll(history.sortedDescending()) // latest first
                    myAdapter.notifyDataSetChanged()
                    noHistoryText.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                noHistoryText.text = "Failed to load history."
                noHistoryText.visibility = View.VISIBLE
            }
    }

}
