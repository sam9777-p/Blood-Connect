package com.example.blood.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blood.repository.HomeRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = HomeRepository()

    private val _bloodGroup = MutableLiveData<String>()
    val bloodGroup: LiveData<String> get() = _bloodGroup

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _userId = MutableLiveData<String>()
    val userId: LiveData<String> get() = _userId

    fun fetchUserData() {
        viewModelScope.launch {
            val data = repository.getUserData()
            _bloodGroup.value = data["bloodGroup"] ?: "N/A"
            _userName.value = data["firstName"] ?: "User"
            _userId.value = data["userId"] ?: ""
        }
    }
}
