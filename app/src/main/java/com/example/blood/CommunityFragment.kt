package com.example.blood

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommunityFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private lateinit var postList: MutableList<Post>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        recyclerView = view.findViewById(R.id.postsRecyclerView)
        val fab: FloatingActionButton = view.findViewById(R.id.fabCreatePost)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postList = mutableListOf()
        adapter = PostAdapter(postList)
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            startActivity(Intent(requireContext(), CreatePostActivity::class.java))
        }

        fetchPostsFromFirestore()
        return view
    }

    private fun fetchPostsFromFirestore() {
        FirebaseFirestore.getInstance().collection("Posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    postList.clear()
                    for (doc in snapshot.documents) {
                        doc.toObject(Post::class.java)?.let { postList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
