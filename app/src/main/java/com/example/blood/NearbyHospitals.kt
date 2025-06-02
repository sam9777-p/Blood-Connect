package com.example.blood

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blood.adapters.HospitalAdapter
import com.example.blood.data.HospitalsData
import com.example.blood.viewmodel.NearbyHospitalsViewModel


class NearbyHospitals : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HospitalAdapter
    private val hospitalList = mutableListOf<HospitalsData>()
    private lateinit var noHospitalsText: TextView

    private val viewModel: NearbyHospitalsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nearby_hospitals, container, false)

        noHospitalsText = view.findViewById(R.id.noHospitalsText)
        recyclerView = view.findViewById(R.id.RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HospitalAdapter(hospitalList)
        recyclerView.adapter = adapter

        checkLocationPermissionAndLoad()

        viewModel.hospitals.observe(viewLifecycleOwner) { hospitals ->
            hospitalList.clear()
            hospitalList.addAll(hospitals)
            adapter.notifyDataSetChanged()
        }

        viewModel.showNoHospitalsText.observe(viewLifecycleOwner) { show ->
            noHospitalsText.visibility = if (show) View.VISIBLE else View.GONE
            recyclerView.visibility = if (show) View.GONE else View.VISIBLE
        }

        return view
    }

    private fun checkLocationPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else {
            viewModel.fetchCityAndLoadHospitals()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.fetchCityAndLoadHospitals()
        }
    }
}
