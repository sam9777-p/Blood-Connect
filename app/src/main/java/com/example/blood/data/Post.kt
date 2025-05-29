package com.example.blood.data

data class Post(
    val userName: String = "",
    val title: String = "",
    val content: String = "",
    val flair: String = "",
    val timestamp: Long = System.currentTimeMillis()
)