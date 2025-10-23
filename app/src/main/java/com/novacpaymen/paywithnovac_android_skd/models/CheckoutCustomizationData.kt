package com.novacpaymen.paywithnovac_android_skd.models

import com.google.gson.annotations.SerializedName

data class CheckoutCustomizationData(
    @SerializedName("logoUrl")
    val logoUrl: String? = null,

    @SerializedName("paymentDescription")
    val paymentDescription: String? = null,

    @SerializedName("checkoutModalTitle")
    val checkoutModalTitle: String? = null
)