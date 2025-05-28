package com.example.blood

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class NearbyHospitals : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HospitalAdapter
    private val hospitalList = mutableListOf<HospitalsData>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var noHospitalsText: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_nearby_hospitals, container, false)
        noHospitalsText = view.findViewById(R.id.noHospitalsText)

        recyclerView = view.findViewById(R.id.RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HospitalAdapter(hospitalList)
        recyclerView.adapter = adapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        checkLocationPermissionAndLoad()

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
            fetchCityAndLoadHospitals()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun fetchCityAndLoadHospitals() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addressList.isNullOrEmpty()) {
                    val city = addressList[0].locality ?: return@addOnSuccessListener
                    val donorPhone = currentUser?.phoneNumber ?: return@addOnSuccessListener

                    // Update donor's city in Firestore
                    db.collection("Accounts").document(donorPhone)
                        .update("city", city)

                    // Now load hospitals in that city
                    loadNearbyHospitals()
                    //loadHospitalsByCity(city)
                }
            }
        }
    }

//    private fun loadHospitalsByCity(city: String) {
//        db.collection("hospitals").get().addOnSuccessListener { hospitalDocs ->
//            val hospitalIds = hospitalDocs.documents.map { it.id }
//
//            // ✅ Prevent crash: ensure hospitalIds is not empty before whereIn()
//            if (hospitalIds.isEmpty()) {
//                hospitalList.clear()
//                adapter.notifyDataSetChanged()
//                return@addOnSuccessListener
//            }
//
//            db.collection("Accounts")
//                .whereIn(FieldPath.documentId(), hospitalIds)
//                .get()
//                .addOnSuccessListener { hospitalInfoDocs ->
//                    hospitalList.clear()
//                    for (doc in hospitalInfoDocs) {
//                        val hospitalCity = doc.getString("city") ?: continue
//                        if (hospitalCity.equals(city, ignoreCase = true)) {
//                            val hospital = HospitalsData(
//                                phoneNumber = doc.id,
//                                hospitalName = doc.getString("hospitalName") ?: "",
//                                address = doc.getString("address") ?: "",
//                                city = hospitalCity,
//                                latitude = doc.getDouble("latitude") ?: 0.0,
//                                longitude = doc.getDouble("longitude") ?: 0.0
//                            )
//                            hospitalList.add(hospital)
//                        }
//                    }
//                    adapter.notifyDataSetChanged()
//                }
//        }
//    }

    private fun loadNearbyHospitals() {
        val donorPhone = currentUser?.phoneNumber ?: return

        db.collection("Accounts").document(donorPhone).get()
            .addOnSuccessListener { donorDoc ->
                val donorCity = donorDoc.getString("city")
                if (donorCity.isNullOrEmpty()) {
                    noHospitalsText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    return@addOnSuccessListener
                }

                // Fetch all hospitals in the same city directly from Accounts
                db.collection("Accounts")
                    .whereEqualTo("role", "Hospital")
                    .whereEqualTo("city", donorCity)
                    .get()
                    .addOnSuccessListener { hospitalDocs ->
                        hospitalList.clear()
                        for (doc in hospitalDocs) {
                            val hospital = HospitalsData(
                                phoneNumber = doc.id,
                                hospitalName = doc.getString("hospitalName") ?: "",
                                address = doc.getString("address") ?: "",
                                city = donorCity,
                                latitude = doc.getDouble("latitude") ?: 0.0,
                                longitude = doc.getDouble("longitude") ?: 0.0
                            )
                            hospitalList.add(hospital)
                        }
                        adapter.notifyDataSetChanged()
                    }
            }
    }


    // Optional: handle permission result
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            fetchCityAndLoadHospitals()
        }
    }
}
