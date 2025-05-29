package com.example.blood.data

data class EligibilityRequest(
    val height: Float  =0f,
    val  weight: Int =0,
    val  gender:String="",
    val  age:Int =0,
    val  hemoglobin:Float=0f
)