package com.example.blood
//
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//
//class RequestViewModel : ViewModel() {
//
//    private val repository = RequestRepository()
//
//    val requestLiveData = MutableLiveData<BloodRequest?>()
//    val errorLiveData = MutableLiveData<String>()
//    val loadingLiveData = MutableLiveData<Boolean>()
//
//    fun fetchCurrentRequest(phone: String) {
//        loadingLiveData.value = true
//        repository.getCurrentRequest(phone) { request ->
//            loadingLiveData.value = false
//            requestLiveData.value = request
//        }
//    }
//
//    fun submitRequest(request: BloodRequest) {
//        loadingLiveData.value = true
//        repository.submitRequest(request, {
//            loadingLiveData.value = false
//            fetchCurrentRequest(request.requester)
//        }, {
//            loadingLiveData.value = false
//            errorLiveData.value = it.message
//        })
//    }
//}
