package com.novacpaymen.paywithnovac_android_skd.models

import com.google.gson.annotations.SerializedName

data class TransactionData(
    @SerializedName("transactionReference")
    val transactionReference: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("statusCode")
    val statusCode: String,

    @SerializedName("statusMessage")
    val statusMessage: String,

    @SerializedName("publicKey")
    val publicKey: String,

    @SerializedName("paymentRedirectUrl")
    val paymentRedirectUrl: String,

    @SerializedName("collectionPaymentOptions")
    val collectionPaymentOptions: String
)