package com.novacpaymen.paywithnovac_android_skd.models

import com.google.gson.annotations.SerializedName

data class InitiateRequest(
    @SerializedName("transactionReference")
    val transactionReference: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("checkoutCustomerData")
    val checkoutCustomerData: CheckoutCustomerData, // Changed from customerData to checkoutCustomerData

    @SerializedName("checkoutCustomizationData")
    val checkoutCustomizationData: CheckoutCustomizationData? = null // Changed from customizationData to checkoutCustomizationData
)