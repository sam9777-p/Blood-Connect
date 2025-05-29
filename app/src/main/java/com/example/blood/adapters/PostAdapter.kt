package com.example.blood.adapters

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blood.data.Post
import com.example.blood.R

class PostAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userIdText: TextView = view.findViewById(R.id.userIdText)
        val titleText: TextView = view.findViewById(R.id.titleText)
        val contentText: TextView = view.findViewById(R.id.contentText)
        val timestampText: TextView = view.findViewById(R.id.timestampText)
        val postFlair: TextView = view.findViewById(R.id.postFlair)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.postFlair.text = post.flair
        holder.userIdText.text = post.userName
        holder.titleText.text = post.title
        holder.contentText.text = post.content
        holder.timestampText.text = DateFormat.format("dd MMM yyyy, HH:mm", post.timestamp).toString()
    }

    override fun getItemCount(): Int = posts.size
}