package com.example.blood

import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.blood.viewmodel.PostViewModel

class CreatePostActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var contentInput: EditText
    private lateinit var createButton: Button
    private val postViewModel: PostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        val flairInput = findViewById<Spinner>(R.id.flairSpinner)
        titleInput = findViewById(R.id.postTitleInput)
        contentInput = findViewById(R.id.postContentInput)
        createButton = findViewById(R.id.createPostButton)

        postViewModel.postCreationStatus.observe(this, Observer { result ->
            result.onSuccess {
                Toast.makeText(this, "Post created", Toast.LENGTH_SHORT).show()
                finish()
            }
            result.onFailure {
                Toast.makeText(this, "Failed to post: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        })

        createButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val content = contentInput.text.toString().trim()
            val flair = flairInput.selectedItem.toString()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            postViewModel.createPost(title, content, flair)
        }
    }
}
