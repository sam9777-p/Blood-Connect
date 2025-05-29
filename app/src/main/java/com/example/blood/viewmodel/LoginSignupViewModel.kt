package com.example.blood.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.MutableLiveData

class LoginSignupViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val KEY_PHONE_NUMBER = "phone_number"
    }

    val phoneNumber = savedStateHandle.getLiveData<String>(KEY_PHONE_NUMBER)

    fun setPhoneNumber(number: String) {
        savedStateHandle[KEY_PHONE_NUMBER] = number
    }
}
