package com.example.blood.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blood.repository.DonorHistoryRepository
import kotlinx.coroutines.launch

class DonorHistoryViewModel : ViewModel() {

    private val repository = DonorHistoryRepository()

    private val _historyList = MutableLiveData<List<String>>()
    val historyList: LiveData<List<String>> = _historyList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchDonationHistory() {
        viewModelScope.launch {
            repository.getDonationHistory(
                onSuccess = { history ->
                    _historyList.postValue(history)
                },
                onFailure = { error ->
                    _errorMessage.postValue("Failed to load history.")
                }
            )
        }
    }
}
