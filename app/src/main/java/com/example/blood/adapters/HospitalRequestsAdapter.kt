package com.example.blood.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blood.data.BloodRequest
import com.example.blood.R

class HospitalRequestsAdapter(
    private val requestList: List<BloodRequest>,
    private val onAccept: (BloodRequest) -> Unit,
    private val onReject: (BloodRequest) -> Unit
) : RecyclerView.Adapter<HospitalRequestsAdapter.RequestViewHolder>() {


    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRequesterName: TextView = itemView.findViewById(R.id.tvRequesterName)
        val tvBloodGroup: TextView = itemView.findViewById(R.id.tvBloodGroup)
        val tvUnitsNeeded: TextView = itemView.findViewById(R.id.tvUnitsNeeded)
        val tvRequestTime: TextView = itemView.findViewById(R.id.tvRequestTime)
        val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blood_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestList[position]
        holder.tvRequesterName.text = "Requested by: ${request.requesterName}"
        holder.tvBloodGroup.text = "Blood Group: ${request.bloodGroup}"
        holder.tvUnitsNeeded.text = "Units Needed: ${request.units}"
        holder.tvRequestTime.text = "Requested: ${request.timestamp?.toDate()?.toString() ?: "Unknown"}"

        holder.btnAccept.setOnClickListener { onAccept(request) }
        holder.btnReject.setOnClickListener { onReject(request) }
    }

    override fun getItemCount(): Int = requestList.size
}