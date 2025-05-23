package com.example.blood

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var phoneText: TextView
    private lateinit var countdownText: TextView
    private lateinit var changeText: TextView
    private lateinit var otpInput: EditText
    private lateinit var verifyButton: Button
    private lateinit var resendOtpButton: Button
    private var verificationId: String? = null
    private lateinit var countdownTimer: CountDownTimer
    private lateinit var phoneNumber: String

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
        phoneText.text = phoneNumber
        val countryIso = intent.getStringExtra("countryIso")

        val flagImage = findViewById<ImageView>(R.id.flagImage)
        countryIso?.let {
            val resId = resources.getIdentifier("ic_flag_$it", "drawable", packageName)
            if (resId != 0) {
                flagImage.setImageResource(resId)
            } else {
                flagImage.setImageResource(R.drawable.ic_flag_india) // fallback flag
            }
        }

        // Firebase phone auth
        startPhoneNumberVerification(phoneNumber)

        changeText.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            onBackPressed()
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
            // Resend OTP when button is clicked
            startPhoneNumberVerification(phoneNumber)
            startCountdown()  // Reset the countdown timer
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
            startCountdown()  // Start the countdown when OTP is sent}

        }
        catch (e: Exception) {
            Log.d("OtpVerificationActivity", "Error starting phone number verification: ${e.message}")
        }
    }

    private fun startCountdown() {
        countdownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countdownText.text = "Resend OTP in 00:${(millisUntilFinished / 1000).toString().padStart(2, '0')}"
            }

            override fun onFinish() {
                countdownText.text = "You can resend OTP now"
                resendOtpButton.isEnabled = true
                resendOtpButton.setTextColor(Color.parseColor("#0066CC"))  // Change text color when enabled
            }
        }
        countdownTimer.start()
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            val code = credential.smsCode
            if (code != null) {
                otpInput.setText(code)
                signInWithPhoneAuthCredential(credential)
            }
        }


        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@OtpVerificationActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
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

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer.cancel()
    }
}
