package com.example.blood

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.blood.databinding.ActivityCreatePostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreatePostActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var createButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        val flairInput = findViewById<Spinner>(R.id.flairSpinner)
        titleInput = findViewById(R.id.postTitleInput)
        contentInput = findViewById(R.id.postContentInput)
        createButton = findViewById(R.id.createPostButton)

        createButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val content = contentInput.text.toString().trim()
            val flair = flairInput.selectedItem.toString()
            //val user = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: "Anonymous"

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId =  FirebaseAuth.getInstance().currentUser?.phoneNumber ?: return@setOnClickListener
            FirebaseFirestore.getInstance().collection("Accounts").document(userId).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("firstName") ?: "Anonymous"
                    val post = Post(title, content, name,flair, System.currentTimeMillis())

                    FirebaseFirestore.getInstance().collection("Posts").add(post)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Post created", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to post", Toast.LENGTH_SHORT).show()
                        }
                }

        }
    }
}
