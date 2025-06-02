package com.example.blood.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blood.repository.HospitalProfileRepository

class HospitalProfileViewModel : ViewModel() {

    private val repository = HospitalProfileRepository()

    private val _profileData = MutableLiveData<Map<String, Any>>()
    val profileData: LiveData<Map<String, Any>> = _profileData

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadProfile() {
        repository.loadProfile(
            onSuccess = { data -> _profileData.value = data },
            onFailure = { e -> _error.value = e.message }
        )
    }

    fun saveProfile(
        name: String,
        city: String,
        contact: String,
        address: String,
        location: Location?
    ) {
        repository.saveProfile(
            name, city, contact, address, location,
            onSuccess = { _saveSuccess.value = true },
            onFailure = { e -> _error.value = e.message }
        )
    }
}
