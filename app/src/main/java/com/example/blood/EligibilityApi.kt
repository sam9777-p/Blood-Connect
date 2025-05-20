package com.example.blood

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

interface EligibilityApi {
    @Headers("Content-Type: application/json")
    @POST("/predict")
    fun analyzeEligibility(@Body eligibilityRequest: EligibilityRequest): Call<EligibilityResponse>
}