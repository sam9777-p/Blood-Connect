package com.example.blood.viewmodel

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class OtpVerificationViewModel(application: Application) : AndroidViewModel(application) {

    private val _otp = MutableLiveData<String>()
    val otp: LiveData<String> get() = _otp

    private val _countdownText = MutableLiveData<String>()
    val countdownText: LiveData<String> get() = _countdownText

    private val _resendEnabled = MutableLiveData(false)
    val resendEnabled: LiveData<Boolean> get() = _resendEnabled

    private var countdownTimer: CountDownTimer? = null

    fun updateOtp(code: String) {
        _otp.value = code
    }

    fun startCountdown(durationMillis: Long = 30_000) {
        countdownTimer?.cancel()
        _resendEnabled.value = false
        countdownTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                _countdownText.value = "Resend OTP in 00:${seconds.toString().padStart(2, '0')}"
            }

            override fun onFinish() {
                _countdownText.value = "You can resend OTP now"
                _resendEnabled.value = true
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        countdownTimer?.cancel()
    }
}
