package com.example.blood

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.hbb20.CountryCodePicker

class LoginSignupActivity : AppCompatActivity() {

    private lateinit var ccp: CountryCodePicker
    private lateinit var phoneEditText: EditText
    private lateinit var continueBtn: Button
    private lateinit var signInClient: SignInClient

    private val phoneNumberHintLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val numberFromHint = result.data?.getStringExtra("android.intent.extra.PHONE_NUMBER")
                ?: result.data?.getStringExtra("phone_number")

            numberFromHint?.let {
                Log.d("PhoneHint", "Received number: $it")
                val localNumber = it.replace(Regex("[^0-9]"), "")
                val strippedNumber = if (localNumber.startsWith("91") && localNumber.length > 10) {
                    localNumber.substring(2)
                } else {
                    localNumber
                }
                phoneEditText.setText(strippedNumber)
                phoneEditText.setSelection(strippedNumber.length)
                ccp.setFullNumber("+" + ccp.selectedCountryCode + strippedNumber)
                continueBtn.visibility = if (strippedNumber.length in 9..12) View.VISIBLE else View.GONE
            }
                ?: Log.e("PhoneHint", "No number returned from hint picker")
        } else {
            Log.d("PhoneHint", "User cancelled phone hint dialog")
        }

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)
        enableEdgeToEdge()
        ccp = findViewById(R.id.countryCodePicker)
        phoneEditText = findViewById(R.id.editTextPhone)
        continueBtn = findViewById(R.id.btnContinue)

        ccp.registerCarrierNumberEditText(phoneEditText)

        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().trim()
                continueBtn.visibility = if (phone.length in 9..12) View.VISIBLE else View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        continueBtn.setOnClickListener {
            val phoneNumber = ccp.fullNumberWithPlus
            if (phoneNumber.isNotEmpty()) {
                val countryIso = ccp.selectedCountryNameCode.lowercase()
                val intent = Intent(this, OtpVerificationActivity::class.java)
                intent.putExtra("phoneNumber", phoneNumber)
                intent.putExtra("countryIso", countryIso)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Enter valid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        showPhoneNumberHint()
    }

    private fun showPhoneNumberHint() {
        signInClient = Identity.getSignInClient(this)
        val request = GetPhoneNumberHintIntentRequest.builder().build()

        signInClient.getPhoneNumberHintIntent(request)
            .addOnSuccessListener { pendingIntent ->
                val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent).build()
                phoneNumberHintLauncher.launch(intentSenderRequest)
            }
            .addOnFailureListener {
                Log.e("PhoneHint", "Failed to show hint: ${it.message}")
            }
    }
}
