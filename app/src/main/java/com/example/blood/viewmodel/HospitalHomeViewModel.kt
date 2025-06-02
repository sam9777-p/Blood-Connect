package com.example.blood.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blood.data.InventoryItem
import com.example.blood.repository.HospitalHomeRepository

class HospitalHomeViewModel : ViewModel() {

    private val repository = HospitalHomeRepository()

    private val _inventoryItems = MutableLiveData<List<InventoryItem>>()
    val inventoryItems: LiveData<List<InventoryItem>> = _inventoryItems

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun fetchInventory() {
        repository.fetchInventory(_inventoryItems, _loading)
    }
}
