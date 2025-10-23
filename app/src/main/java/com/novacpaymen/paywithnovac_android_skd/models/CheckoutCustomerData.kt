package com.novacpaymen.paywithnovac_android_skd.models

import com.google.gson.annotations.SerializedName

data class CheckoutCustomerData(
    @SerializedName("email")
    val email: String,

    @SerializedName("firstName")
    val firstName: String? = null,

    @SerializedName("lastName")
    val lastName: String? = null,

    @SerializedName("phoneNumber")
    val phoneNumber: String? = null
)