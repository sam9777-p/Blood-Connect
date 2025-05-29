package com.example.blood.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blood.data.Post
import com.example.blood.repository.PostRepository
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    private val repository = PostRepository()

    private val _postCreationStatus = MutableLiveData<Result<Unit>>()
    val postCreationStatus: LiveData<Result<Unit>> = _postCreationStatus

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    fun createPost(title: String, content: String, flair: String) {
        viewModelScope.launch {
            val result = repository.createPost(title, content, flair)
            _postCreationStatus.value = result
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            val result = repository.fetchPosts()
            result.onSuccess {
                _posts.value = it.sortedByDescending { post -> post.timestamp }
            }
        }
    }
}