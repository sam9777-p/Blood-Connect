package com.example.blood

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.blood.InventoryAdapter.InventoryViewHolder

import com.google.android.material.imageview.ShapeableImageView

class HistoryAdapter (private val list: List<String>) :
    RecyclerView.Adapter<HistoryAdapter.MyViewHolder>(){

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_donation_date, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var title : TextView= itemView.findViewById(R.id.dateTextView)
    }


}