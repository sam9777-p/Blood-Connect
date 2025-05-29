package com.example.blood.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blood.R

class HistoryAdapter (private var list: List<String>) :
    RecyclerView.Adapter<HistoryAdapter.MyViewHolder>(){

    fun updateList(newList: List<String>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_donation_date, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = list[position]
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_fade_in)
        holder.itemView.startAnimation(animation)
//        if (position == list.lastIndex) {
//            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.gold_highlight))
//        } else {
//            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.default_bg))
//        }

    }

    override fun getItemCount(): Int {
        return list.size
    }


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var title : TextView = itemView.findViewById(R.id.dateTextView)
    }


}