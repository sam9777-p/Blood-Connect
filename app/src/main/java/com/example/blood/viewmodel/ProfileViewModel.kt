package com.example.blood.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blood.repository.ProfileRepository
import com.google.android.gms.location.FusedLocationProviderClient

class ProfileViewModel(fusedLocationClient: FusedLocationProviderClient, context: Context) : ViewModel() {
    private val repository = ProfileRepository(fusedLocationClient, context)

    private val _profileData = MutableLiveData<Map<String, String?>>()
    val profileData: LiveData<Map<String, String?>> = _profileData

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun loadProfile() {
        repository.loadProfile { data ->
            _profileData.value = data
        }
    }

    fun saveUserProfile(
        bloodGroup: String,
        gender: String,
        ageStr: String,
        weightStr: String,
        heightStr: String,
        haemoglobinStr: String,
        donationDate: String
    ) {
        repository.saveUserProfile(
            bloodGroup, gender, ageStr, weightStr, heightStr, haemoglobinStr, donationDate
        ) { success ->
            _saveSuccess.value = success
        }
    }
}
