package com.example.blood

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blood.adapters.HistoryAdapter
import com.example.blood.viewmodel.DonorHistoryViewModel

class DonorHistoryFragment : Fragment(R.layout.fragment_donor_history) {

    private lateinit var recycler: RecyclerView
    private lateinit var myAdapter: HistoryAdapter
    private lateinit var noHistoryText: TextView
    private val viewModel: DonorHistoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler = view.findViewById(R.id.donationRecyclerView)
        noHistoryText = view.findViewById(R.id.noHistoryText)

        myAdapter = HistoryAdapter(emptyList())
        recycler.adapter = myAdapter
        recycler.layoutManager = LinearLayoutManager(requireContext())

        viewModel.historyList.observe(viewLifecycleOwner, Observer { history ->
            if (history.isNullOrEmpty()) {
                noHistoryText.visibility = View.VISIBLE
                recycler.visibility = View.GONE
            } else {
                noHistoryText.visibility = View.GONE
                recycler.visibility = View.VISIBLE
                myAdapter.updateList(history.sortedDescending())
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            noHistoryText.text = error
            noHistoryText.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        })

        viewModel.fetchDonationHistory()
    }
}
