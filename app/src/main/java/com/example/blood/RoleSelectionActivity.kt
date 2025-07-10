package com.example.blood

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.blood.ui.donor.DonorActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var flagImage: ImageView
    private lateinit var phoneText: TextView
    private lateinit var changeText: TextView
    private lateinit var emailInput: EditText
    private lateinit var roleDropdown : AutoCompleteTextView
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        // Initialize views
        flagImage = findViewById(R.id.flagImage)
        phoneText = findViewById(R.id.phoneText)
        changeText = findViewById(R.id.changeText)
        emailInput = findViewById(R.id.emailInput)
        roleDropdown = findViewById(R.id.roleDropdown)
        firstNameInput = findViewById(R.id.firstName)
        lastNameInput = findViewById(R.id.lastName)

        // Receive phone number and country info
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: "+91 9870933456"
        val countryCode = intent.getStringExtra("countryCode") ?: "IN"
        phoneText.text = phoneNumber

        when (countryCode.uppercase()) {
            "IN" -> flagImage.setImageResource(R.drawable.ic_flag_india)
            "US" -> flagImage.setImageResource(R.drawable.ic_flag_us)
            "UK" -> flagImage.setImageResource(R.drawable.ic_flag_uk)
            else -> flagImage.setImageResource(R.drawable.ic_flag_india)
        }

        // Spinner setup for role dropdown
        val roles = arrayOf("Donor", "Hospital")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        roleDropdown.setAdapter(adapter)

        // Setup Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Trigger sign-in flow immediately
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            emailInput.setText(account.email)
        } else {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        //emailInput.isEnabled = false

        // Change phone number action
        changeText.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            googleSignInClient.signOut()
            val intent = Intent(this, LoginSignupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // Submit the role selection
        val submitButton = findViewById<Button>(R.id.continueButton) // Add a submit button for role selection
        submitButton.setOnClickListener {
            val selectedRole = roleDropdown.text.toString()

            // Validate inputs
            if (firstNameInput.text.isEmpty() || lastNameInput.text.isEmpty()) {
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedRole.isEmpty()) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save user information in Firestore
            saveUserInformationToFirestore(phoneNumber, selectedRole)
        }
    }

    private fun saveUserInformationToFirestore(phoneNumber: String, role: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return
        // Get user details
        val user = hashMapOf(
            "phoneNumber" to phoneNumber,
            "email" to emailInput.text.toString(),
            "firstName" to firstNameInput.text.toString(),
            "lastName" to lastNameInput.text.toString(),
            "role" to role
        )

        // Store in Firestore under the "Accounts" collection
        db.collection("Accounts")
            .document(userId) // Use phone number as the document ID
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User information saved", Toast.LENGTH_SHORT).show()

                // Redirect to the appropriate activity based on role
                if (role == "Hospital") {
                    val intent = Intent(this, HospitalActivity::class.java)
                    startActivity(intent)
                } else if (role == "Donor") {
                    val intent = Intent(this, DonorActivity::class.java)
                    startActivity(intent)
                }
                finish() // Close this activity
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save user information", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.let {
                // Fill email
                emailInput.setText(it.email ?: "")

                // Extract and fill name
                val fullName = it.displayName ?: ""
                val nameParts = fullName.split(" ")
                if (nameParts.isNotEmpty()) {
                    firstNameInput.setText(nameParts[0])
                    if (nameParts.size > 1) {
                        lastNameInput.setText(nameParts.subList(1, nameParts.size).joinToString(" "))
                    }
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onStart() {
        super.onStart()
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val db = FirebaseFirestore.getInstance()
        db.collection("Accounts").document(phoneNumber.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role == "Hospital") {
                        startActivity(Intent(this, HospitalActivity::class.java))
                    } else if (role == "Donor") {
                        startActivity(Intent(this, DonorActivity::class.java))
                    }
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to check account", Toast.LENGTH_SHORT).show()
            }
    }
}
