package com.example.blood

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class QrCodeViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _qrBitmap = MutableLiveData<Bitmap>()
    val qrBitmap: LiveData<Bitmap> get() = _qrBitmap

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _userPhone = MutableLiveData<String>()
    val userPhone: LiveData<String> get() = _userPhone

    private val _bloodGroup = MutableLiveData<String>()
    val bloodGroup: LiveData<String> get() = _bloodGroup

    init {
        loadUserDataAndGenerateQr()
    }

    private fun loadUserDataAndGenerateQr() {
        val phoneNumber = auth.currentUser?.phoneNumber ?: return

        firestore.collection("Accounts").document(phoneNumber).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("firstName") ?: "User"
                    val bloodGrp = doc.getString("bloodGroup") ?: "N/A"
                    _userName.value = name
                    _userPhone.value = phoneNumber
                    _bloodGroup.value = bloodGrp

                    val qrData = "$name\n$phoneNumber\n$bloodGrp"
                    generateQrCode(qrData)
                }
            }
    }

    private fun generateQrCode(data: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 600, 600)
            _qrBitmap.value = bitmap
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
