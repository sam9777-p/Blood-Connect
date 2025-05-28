package com.example.blood

data class BloodRequest(
    val id: String = "",
    val requesterName: String = "",
    val bloodGroup: String = "",
    val units: Int = 0,
    val requestedAt: String = "",
    val city: String = "",
    val status: String = "pending",
    val requesterPhone: String = "",
    val hospitalId: String = "",
    var timestamp: com.google.firebase.Timestamp? = null,
    val donorFcmToken : String = "",
    val interestedDonors: List<String> = emptyList(),
    val handledBy: String = "",

)
