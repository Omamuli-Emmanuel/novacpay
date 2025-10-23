package com.novacpaymen.paywithnovac_android_skd.models

import com.google.gson.annotations.SerializedName

data class InitiateResponse(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: TransactionData?
) {
    // Convenience properties to maintain compatibility
    val success: Boolean get() = status
    val checkoutUrl: String? get() = data?.paymentRedirectUrl
    val transactionId: String? get() = data?.transactionReference
    val reference: String? get() = data?.transactionReference
}

