package com.example.blood.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blood.repository.DonorRepository

class DonorViewModel : ViewModel() {

    private val repository = DonorRepository()

    private val _userData = MutableLiveData<Map<String, String?>>()
    val userData: LiveData<Map<String, String?>> get() = _userData

    private val _isProfileComplete = MutableLiveData<Boolean>()
    val isProfileComplete: LiveData<Boolean> get() = _isProfileComplete

    fun loadUserData() {
        repository.fetchUserData {
            _userData.value = it
        }
    }

    fun checkProfileStatus() {
        repository.checkUserProfile {
            _isProfileComplete.value = it
        }
    }

    fun logout() = repository.logout()
}
