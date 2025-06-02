package com.example.blood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blood.adapters.InventoryAdapter
import com.example.blood.data.InventoryItem
import com.example.blood.viewmodel.HospitalHomeViewModel
import com.facebook.shimmer.ShimmerFrameLayout

class HospitalHomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var inventoryAdapter: InventoryAdapter

    private val viewModel: HospitalHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hospital_home, container, false)
        shimmerLayout = view.findViewById(R.id.shimmerLayout)
        recyclerView = view.findViewById(R.id.inventoryRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        inventoryAdapter = InventoryAdapter(mutableListOf()) { bloodGroup ->
            val bottomSheet = GroupDetailsBottomSheet.newInstance(bloodGroup)
            bottomSheet.show(parentFragmentManager, "GroupDetailsBottomSheet")
        }

        recyclerView.adapter = inventoryAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        viewModel.fetchInventory()
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) showShimmer() else hideShimmer()
        })

        viewModel.inventoryItems.observe(viewLifecycleOwner, Observer { inventory ->
            inventoryAdapter.updateData(inventory)
        })
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
}
