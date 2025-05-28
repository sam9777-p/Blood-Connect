package com.example.blood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class BannerAdapter(private val items: List<Int>) :
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

    private val extendedItems = listOf(items.last()) + items + listOf(items.first())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(extendedItems[position])
    }

    override fun getItemCount(): Int = extendedItems.size

    inner class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(imageResId: Int) {
            itemView.findViewById<ImageView>(R.id.bannerImageView).setImageResource(imageResId)
        }
    }
}

