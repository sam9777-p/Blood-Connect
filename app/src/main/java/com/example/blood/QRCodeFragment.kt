package com.example.blood

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import org.json.JSONObject

class QRCodeFragment : Fragment() {

    private lateinit var qrImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_q_r_code, container, false)
        qrImageView = view.findViewById(R.id.qrCodeImage)
        generateAndDisplayQRCode()
        return view
    }

    private fun generateAndDisplayQRCode() {
        val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("Accounts").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val bloodGroup = document.getString("bloodGroup") ?: "Unknown"
                val eligibility = document.getString("eligibility") ?: "Unknown"
                val date = document.getString("lastDonationDate") ?: "Unknown"
                val qrData = JSONObject(
                    mapOf(
                        "phoneNumber" to userId,
                        "bloodGroup" to bloodGroup,
                        "eligibility" to eligibility,
                        "lastDonationDate" to date
                    )
                ).toString()

                val qrBitmap = generateQRCode(qrData)
                qrImageView.setImageBitmap(qrBitmap)
            }
        }
    }

    private fun generateQRCode(text: String): Bitmap {
        val writer = com.google.zxing.qrcode.QRCodeWriter()
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
