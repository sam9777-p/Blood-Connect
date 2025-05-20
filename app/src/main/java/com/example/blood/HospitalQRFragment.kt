package com.example.blood

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.Manifest
import android.util.Log

import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis

import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors


class HospitalQRFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }

    private var isProcessingQR = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hospital_q_r, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        previewView = view.findViewById(R.id.previewView)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is needed to scan QR", Toast.LENGTH_LONG).show()
            }
        }

// Request permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        //startCamera()
    }

    /*@RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is needed to scan QR", Toast.LENGTH_LONG).show()
            }
        }
    }*/


    @RequiresApi(Build.VERSION_CODES.O)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, QRAnalyzer { qrData ->
                    handleScannedQR(qrData)
                })
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, analyzer)
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleScannedQR(data: String) {
        if (isProcessingQR) return
        isProcessingQR = true

        try {
            val json = JSONObject(data)
            val phone = json.optString("phoneNumber", "")
            val bloodGroup = json.optString("bloodGroup", "")
            val eligibility = json.optString("eligibility", "")
            val lastDonationDateStr = json.optString("lastDonationDate", "")

            val today = LocalDate.now()

            // 1. Check BMI-based eligibility
            if (eligibility != "Eligible") {
                Toast.makeText(requireContext(), "Donor not eligible based on BMI", Toast.LENGTH_LONG).show()
                isProcessingQR = false
                return
            }

            // 2. Check last donation date (56 days rule)
            if (lastDonationDateStr.isNotEmpty() && lastDonationDateStr != "Unknown") {
                val formatter = DateTimeFormatter.ofPattern("d/M/yyyy", Locale.getDefault())
                val cleanDateStr = lastDonationDateStr.trim()

                val lastDonationDate = runCatching {
                    LocalDate.parse(cleanDateStr, formatter)
                }.getOrElse {
                    Log.e("HospitalQRFragment", "Date parsing failed: '$cleanDateStr'", it)
                    Toast.makeText(requireContext(), "Invalid date in QR", Toast.LENGTH_SHORT).show()
                    isProcessingQR = false
                    return
                }

                val daysSinceLastDonation = java.time.temporal.ChronoUnit.DAYS.between(lastDonationDate, today)


                if (daysSinceLastDonation < 56) {
                    Toast.makeText(requireContext(), "Donor not eligible. Only $daysSinceLastDonation days since last donation.", Toast.LENGTH_LONG).show()
                    isProcessingQR = false
                    return
                }
            }

            // If both checks pass, record donation
            val currentDate = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()))

            updateHospitalInventory(bloodGroup,phone)

            val userDocRef = FirebaseFirestore.getInstance()
                .collection("Accounts")
                .document(phone)

            userDocRef.update(
                mapOf(
                    "lastDonationDate" to currentDate,
                    "donationHistory" to FieldValue.arrayUnion(currentDate)
                )
            ).addOnSuccessListener {
                Toast.makeText(requireContext(), "Donation recorded successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update donor", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Invalid QR", Toast.LENGTH_SHORT).show()
            }
        }

        previewView.postDelayed({
            isProcessingQR = false
        }, 5000)
    }



    private fun updateHospitalInventory(bloodGroup: String,phone: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return
        val inventoryRef = FirebaseFirestore.getInstance()
            .collection("hospitals")
            .document(userId)
            .collection("inventory")
            .document(bloodGroup)

        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(inventoryRef)
            val currentUnits = snapshot.getLong("units") ?: 0
            val newUnits = currentUnits + 1

            val newStatus = when {
                newUnits < 5 -> "Low"
                newUnits < 10 -> "Medium"
                else -> "High"
            }

            transaction.update(inventoryRef, mapOf(
                "units" to newUnits,
                "status" to newStatus,
                "Donors" to FieldValue.arrayUnion(phone)
            ))
            Log.d("HospitalQRFragment", "Adding donor phone to inventory: $phone")

        }.addOnSuccessListener {
            Toast.makeText(requireContext(), "Inventory updated for $bloodGroup", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to update inventory", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // QR Analyzer Class

}
