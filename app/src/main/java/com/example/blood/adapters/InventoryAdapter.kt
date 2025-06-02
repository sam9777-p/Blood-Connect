package com.example.blood.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blood.data.InventoryItem
import com.example.blood.R

class InventoryAdapter(
    private val items: MutableList<InventoryItem>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    inner class InventoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bloodGroupText: TextView = view.findViewById(R.id.bloodTypeText)
        val unitText: TextView = view.findViewById(R.id.unitCountText)
        val statusText: TextView = view.findViewById(R.id.statusText)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(items[position].bloodGroup)
                }
            }
        }
    }

    fun updateData(newList: List<InventoryItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = items[position]
        holder.bloodGroupText.text = item.bloodGroup
        holder.unitText.text = "${item.units} units"
        holder.statusText.text = item.status
        holder.statusText.setTextColor(
            when (item.status) {
                "Low" -> Color.RED
                "Medium" -> Color.parseColor("#FFA500")
                "High" -> Color.GREEN
                else -> Color.GRAY
            }
        )
    }

    override fun getItemCount() = items.size
}
