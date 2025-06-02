package com.example.blood

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.blood.viewmodel.OtpVerificationViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var phoneText: TextView
    private lateinit var countdownText: TextView
    private lateinit var changeText: TextView
    private lateinit var otpInput: EditText
    private lateinit var verifyButton: Button
    private lateinit var resendOtpButton: Button
    private lateinit var phoneNumber: String
    private var verificationId: String? = null

    private val viewModel: OtpVerificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        phoneText = findViewById(R.id.phoneText)
        countdownText = findViewById(R.id.countdownText)
        changeText = findViewById(R.id.changeText)
        otpInput = findViewById(R.id.otpInput)
        verifyButton = findViewById(R.id.verifyButton)
        resendOtpButton = findViewById(R.id.smsOtpButton)

        phoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        val countryIso = intent.getStringExtra("countryIso")
        phoneText.text = phoneNumber

        val flagImage = findViewById<ImageView>(R.id.flagImage)
        countryIso?.let {
            val resId = resources.getIdentifier("ic_flag_$it", "drawable", packageName)
            flagImage.setImageResource(if (resId != 0) resId else R.drawable.ic_flag_india)
        }

        startPhoneNumberVerification(phoneNumber)

        // 🔁 LiveData Observers
        viewModel.countdownText.observe(this, Observer {
            countdownText.text = it
        })

        viewModel.resendEnabled.observe(this, Observer { enabled ->
            resendOtpButton.isEnabled = enabled
            resendOtpButton.setTextColor(
                if (enabled) Color.parseColor("#0066CC")
                else Color.parseColor("#AAAAAA")
            )
        })

        viewModel.otp.observe(this, Observer { code ->
            if (otpInput.text.toString() != code) {
                otpInput.setText(code)
            }
        })

        // 💬 OTP input tracking
        otpInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                viewModel.updateOtp(otpInput.text.toString())
            }
        }

        changeText.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            onBackPressedDispatcher.onBackPressed()
        }

        verifyButton.setOnClickListener {
            val otp = otpInput.text.toString()
            if (otp.isNotEmpty() && verificationId != null) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }

        resendOtpButton.setOnClickListener {
            startPhoneNumberVerification(phoneNumber)
            viewModel.startCountdown()
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        try {
            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNumber)
                .setTimeout(30L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
            viewModel.startCountdown()
        } catch (e: Exception) {
            Log.d("OtpVerificationActivity", "Error starting phone number verification: ${e.message}")
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            val code = credential.smsCode
            if (code != null) {
                viewModel.updateOtp(code)
                signInWithPhoneAuthCredential(credential)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@OtpVerificationActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("Error sigin", e.message.toString())
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            this@OtpVerificationActivity.verificationId = verificationId
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Verified Successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, RoleSelectionActivity::class.java)
                    intent.putExtra("phoneNumber", phoneNumber)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Verification Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
