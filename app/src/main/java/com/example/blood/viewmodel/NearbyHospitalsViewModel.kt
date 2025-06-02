package com.example.blood.viewmodel

import android.Manifest
import android.app.Application
import android.location.Geocoder
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.blood.data.HospitalsData
import com.example.blood.repository.NearbyHospitalsRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class NearbyHospitalsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val repository = NearbyHospitalsRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _hospitals = MutableLiveData<List<HospitalsData>>()
    val hospitals: LiveData<List<HospitalsData>> = _hospitals

    private val _showNoHospitalsText = MutableLiveData<Boolean>()
    val showNoHospitalsText: LiveData<Boolean> = _showNoHospitalsText

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun fetchCityAndLoadHospitals() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addressList.isNullOrEmpty()) {
                    val city = addressList[0].locality ?: return@addOnSuccessListener
                    val donorPhone = auth.currentUser?.phoneNumber ?: return@addOnSuccessListener

                    // Update donor city
                    repository.updateDonorCity(donorPhone, city)

                    // Load hospitals from Firestore
                    repository.loadHospitalsByCity(city) { hospitalsList ->
                        if (hospitalsList.isEmpty()) {
                            _showNoHospitalsText.postValue(true)
                        } else {
                            _showNoHospitalsText.postValue(false)
                            _hospitals.postValue(hospitalsList)
                        }
                    }
                }
            }
        }
    }
}
