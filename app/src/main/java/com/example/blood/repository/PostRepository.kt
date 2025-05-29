package com.example.blood.repository

import com.example.blood.data.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PostRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun createPost(title: String, content: String, flair: String): Result<Unit> {
        val userId = auth.currentUser?.phoneNumber ?: return Result.failure(Exception("User not logged in"))
        return try {
            val document = db.collection("Accounts").document(userId).get().await()
            val name = document.getString("firstName") ?: "Anonymous"
            val post = Post(title, content, name, flair)
            db.collection("Posts").add(post).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchPosts(): Result<List<Post>> {
        return try {
            val snapshot = db.collection("Posts").get().await()
            val posts = snapshot.toObjects(Post::class.java)
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}