package com.example.blood

data class BloodRequest(
    val id: String = "",
    val name: String = "",
    val bloodGroup: String = "",
    val units: Int = 0,
    val requestedAt: String = "",
    val status: String = "pending",
    val requesterPhone: String = "",
    val handledBy: String? = null
)
