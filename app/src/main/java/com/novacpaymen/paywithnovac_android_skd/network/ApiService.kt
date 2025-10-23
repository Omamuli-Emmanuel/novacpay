package com.novacpaymen.paywithnovac_android_skd.network

import com.novacpaymen.paywithnovac_android_skd.models.InitiateRequest
import com.novacpaymen.paywithnovac_android_skd.models.InitiateResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/v1/initiate")
    suspend fun initiateCheckout(@Body request: InitiateRequest): InitiateResponse
}