package com.example.blood.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blood.data.HospitalsData
import com.example.blood.R

class HospitalAdapter(private val hospitals: List<HospitalsData>) :
    RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    inner class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.hospitalName)
        val addressText: TextView = itemView.findViewById(R.id.hospitalAddress)
        val mapButton: Button = itemView.findViewById(R.id.navigateButton)
        val hospitalCity: TextView = itemView.findViewById(R.id.hospitalCity)
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hospital, parent, false)
        return HospitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        val hospital = hospitals[position]
        holder.nameText.text = hospital.hospitalName
        holder.addressText.text = hospital.address
        holder.hospitalCity.text = hospital.city
        holder.mapButton.setOnClickListener {
            val uri = Uri.parse("geo:${hospital.latitude},${hospital.longitude}?q=${hospital.latitude},${hospital.longitude}(${hospital.hospitalName})")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = hospitals.size
}