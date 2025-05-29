package com.example.blood.viewmodel

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blood.repository.HospitalRepository

class HospitalViewModel : ViewModel() {

    private val repository = HospitalRepository()

    private val _loadHospitalHome = MutableLiveData<Boolean>()
    val loadHospitalHome: LiveData<Boolean> get() = _loadHospitalHome

    private val _locationUpdatedMessage = MutableLiveData<String>()
    val locationUpdatedMessage: LiveData<String> get() = _locationUpdatedMessage

    fun checkIfHospitalProfileExists() {
        repository.doesHospitalProfileExist { exists ->
            _loadHospitalHome.postValue(exists)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun updateUserLocation(context: Context) {
        repository.updateUserLocation(context) { message ->
            _locationUpdatedMessage.postValue(message)
        }
    }
}
