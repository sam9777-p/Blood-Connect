package com.example.blood.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blood.repository.DonorRequestRepository
import com.example.blood.repository.DonorRequestRepository.HospitalData

class DonorRequestViewModel : ViewModel() {

    private val repository = DonorRequestRepository()

    private val _hospitalData = MutableLiveData<HospitalData?>()
    val hospitalData: LiveData<HospitalData?> get() = _hospitalData

    private val _donationStatus = MutableLiveData<Boolean>()
    val donationStatus: LiveData<Boolean> get() = _donationStatus

    fun loadHospitalDetails(hospitalId: String) {
        repository.fetchHospitalDetails(hospitalId).observeForever {
            _hospitalData.value = it
        }
    }

    fun markAsDonating(requestId: String) {
        repository.markAsDonating(requestId).observeForever {
            _donationStatus.value = it
        }
    }
}
