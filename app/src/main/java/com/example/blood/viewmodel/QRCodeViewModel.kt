package com.example.blood.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blood.repository.QRCodeRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject

class QRCodeViewModel : ViewModel() {

    private val repository = QRCodeRepository()

    private val _qrBitmap = MutableLiveData<Bitmap>()
    val qrBitmap: LiveData<Bitmap> = _qrBitmap

    fun fetchQRCode() {
        repository.getUserData { phoneNumber, bloodGroup, eligibility, lastDonationDate ->
            val qrData = JSONObject(
                mapOf(
                    "phoneNumber" to phoneNumber,
                    "bloodGroup" to bloodGroup,
                    "eligibility" to eligibility,
                    "lastDonationDate" to lastDonationDate
                )
            ).toString()
            _qrBitmap.postValue(generateQRCode(qrData))
        }
    }

    private fun generateQRCode(text: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
