package com.novacpaymen.paywithnovac_android_skd

data class CheckoutConfig(
    val merchantId: String,
    val baseUrl: String,
    val apiKey: String,
    val environment: Environment = Environment.PRODUCTION,
    val enableLogging: Boolean = false,
    val successActivityClass: Class<*>? = null,  // Make sure this exists
    val failureActivityClass: Class<*>? = null   // Make sure this exists
) {
    enum class Environment {
        SANDBOX,
        PRODUCTION
    }
}