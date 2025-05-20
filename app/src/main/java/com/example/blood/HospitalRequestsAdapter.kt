package com.example.blood

import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
        holder.tvRequesterName.text = "Requested by: ${request.name}"
        holder.tvBloodGroup.text = "Blood Group: ${request.bloodGroup}"
        holder.tvUnitsNeeded.text = "Units Needed: ${request.units}"
        holder.tvRequestTime.text = "Requested: ${request.requestedAt}" // Format nicely if needed

        holder.btnAccept.setOnClickListener { onAccept(request) }
        holder.btnReject.setOnClickListener { onReject(request) }
    }

    override fun getItemCount(): Int = requestList.size
}
