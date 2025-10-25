package com.novacpaymen.paywithnovac_android_skd

import android.util.Log
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.novacpaymen.paywithnovac_android_skd.models.InitiateRequest
import com.novacpaymen.paywithnovac_android_skd.models.InitiateResponse
import com.novacpaymen.paywithnovac_android_skd.models.CheckoutCustomerData
import com.novacpaymen.paywithnovac_android_skd.models.CheckoutCustomizationData
import com.novacpaymen.paywithnovac_android_skd.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.HttpException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import com.google.gson.Gson

class NovacCheckout private constructor() {

    companion object {
        private var instance: NovacCheckout? = null

        @JvmStatic
        fun initialize(config: CheckoutConfig): NovacCheckout {
            Log.d("NovacSDK", "=== SDK INITIALIZATION STARTED ===")
            Log.d("NovacSDK", "Initializing NovacCheckout SDK with configuration:")
            Log.d("NovacSDK", "  - Merchant ID: ${config.merchantId}")
            Log.d("NovacSDK", "  - Base URL: ${config.baseUrl}")
            Log.d("NovacSDK", "  - Environment: ${config.environment}")
            Log.d("NovacSDK", "  - Logging Enabled: ${config.enableLogging}")

            return instance ?: synchronized(this) {
                instance ?: NovacCheckout().apply {
                    this.config = config
                    initializeRetrofitClient()
                    instance = this
                    Log.d("NovacSDK", "✅ NovacCheckout SDK initialized successfully")
                    Log.d("NovacSDK", "=== SDK INITIALIZATION COMPLETED ===")
                }
            }
        }

        @JvmStatic
        fun getInstance(): NovacCheckout {
            val instance = instance
            if (instance == null) {
                Log.e("NovacSDK", "❌ SDK not initialized. Call NovacCheckout.initialize() first.")
                throw IllegalStateException("NovacCheckout SDK not initialized. Call NovacCheckout.initialize() first.")
            }
            Log.d("NovacSDK", "✅ SDK instance retrieved successfully")
            return instance
        }
    }

    private lateinit var config: CheckoutConfig
    private lateinit var apiService: ApiService

    /**
     * Initialize Retrofit client for API calls
     */
    private fun initializeRetrofitClient() {
        Log.d("NovacSDK", "🔧 Initializing Retrofit client")

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .apply {
                if (config.enableLogging) {
                    val loggingInterceptor = HttpLoggingInterceptor { message ->
                        Log.d("NovacSDK", "🌐 $message")
                    }.apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                    addInterceptor(loggingInterceptor)
                }

                // Add authorization interceptor
                addInterceptor { chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()
                        .header("Authorization", "Bearer ${config.apiKey}")
                        .header("Content-Type", "application/json")

                    val request = requestBuilder.build()
                    chain.proceed(request)
                }
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        Log.d("NovacSDK", "✅ Retrofit client initialized successfully")
    }

    /**
     * Main entry point to initiate checkout - returns checkout URL for WebView
     */
    suspend fun initiateCheckout(
        transactionReference: String,
        amount: Double,
        currency: String,
        checkoutCustomerData: CheckoutCustomerData, // Changed parameter name
        checkoutCustomizationData: CheckoutCustomizationData? = null // Changed parameter name
    ): Result<InitiateResponse> {
        Log.d("NovacSDK", "=== CHECKOUT PROCESS STARTED ===")
        Log.d("NovacSDK", "📦 Building checkout request:")
        Log.d("NovacSDK", "  - Transaction Reference: $transactionReference")
        Log.d("NovacSDK", "  - Amount: $amount $currency")
        Log.d("NovacSDK", "  - Customer Email: ${checkoutCustomerData.email}")
        Log.d("NovacSDK", "  - Customer Name: ${checkoutCustomerData.firstName ?: "N/A"} ${checkoutCustomerData.lastName ?: "N/A"}")
        Log.d("NovacSDK", "  - Customer Phone: ${checkoutCustomerData.phoneNumber ?: "N/A"}")
        Log.d("NovacSDK", "  - Customization Data: ${if (checkoutCustomizationData != null) "Provided" else "Not provided"}")

        return try {
            Log.d("NovacSDK", "🔄 Making API call to initiate checkout...")

            val request = InitiateRequest(
                transactionReference = transactionReference,
                amount = amount,
                currency = currency,
                checkoutCustomerData = checkoutCustomerData, // Updated parameter name
                checkoutCustomizationData = checkoutCustomizationData // Updated parameter name
            )

            // Log exact JSON being sent
            val gson = Gson()
            val requestJson = gson.toJson(request)
            Log.d("NovacSDK", "📋 EXACT JSON BEING SENT:")
            Log.d("NovacSDK", requestJson)

            Log.d("NovacSDK", "📤 Sending request to: ${config.baseUrl}/api/v1/initiate")
            Log.d("NovacSDK", "🔑 Using API Key: ${config.apiKey.take(10)}...")

            val response = withContext(Dispatchers.IO) {
                apiService.initiateCheckout(request)
            }

            Log.d("NovacSDK", "✅ API call completed successfully")
            Log.d("NovacSDK", "📄 Response Details:")
            Log.d("NovacSDK", "  - Status: ${response.status}")
            Log.d("NovacSDK", "  - Message: ${response.message}")

            if (response.status && response.data != null) {
                Log.d("NovacSDK", "  - Transaction Reference: ${response.data.transactionReference}")
                Log.d("NovacSDK", "  - Payment Redirect URL: ${response.data.paymentRedirectUrl}")
                Log.d("NovacSDK", "  - Status Code: ${response.data.statusCode}")
                Log.d("NovacSDK", "  - Status Message: ${response.data.statusMessage}")
                Log.d("NovacSDK", "  - Payment Options: ${response.data.collectionPaymentOptions}")

                Log.d("NovacSDK", "🎉 Checkout process completed successfully")
                Log.d("NovacSDK", "=== CHECKOUT PROCESS COMPLETED ===")
                Result.success(response)
            } else {
                Log.e("NovacSDK", "❌ API returned failure status")
                Log.d("NovacSDK", "  - Error Message: ${response.message}")
                Log.d("NovacSDK", "=== CHECKOUT PROCESS FAILED ===")
                Result.failure(Exception("API Error: ${response.message}"))
            }

        } catch (e: HttpException) {
            Log.e("NovacSDK", "❌ HTTP Error during checkout process: ${e.code()}", e)

            // Try to parse the error response body for more details
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("NovacSDK", "❌ Error Response Body: $errorBody")

            Log.d("NovacSDK", "💥 HTTP Exception details:")
            Log.d("NovacSDK", "  - Status Code: ${e.code()}")
            Log.d("NovacSDK", "  - Message: ${e.message}")
            Log.d("NovacSDK", "=== CHECKOUT PROCESS FAILED ===")

            val errorMessage = if (!errorBody.isNullOrBlank()) {
                "HTTP Error ${e.code()}: $errorBody"
            } else {
                "HTTP Error ${e.code()}: ${e.message}"
            }
            Result.failure(Exception(errorMessage))

        } catch (e: Exception) {
            Log.e("NovacSDK", "❌ Error during checkout process:", e)
            Log.d("NovacSDK", "💥 Exception details:")
            Log.d("NovacSDK", "  - Message: ${e.message}")
            Log.d("NovacSDK", "  - Type: ${e.javaClass.simpleName}")
            Log.d("NovacSDK", "=== CHECKOUT PROCESS FAILED ===")
            Result.failure(e)
        }
    }

    /**
     * Convenience method with simpler parameters
     */
    suspend fun initiateCheckout(
        transactionReference: String,
        amount: Double,
        currency: String,
        customerEmail: String,
        customerFirstName: String? = null,
        customerLastName: String? = null,
        customerPhone: String? = null
    ): Result<InitiateResponse> {

        Log.d("NovacSDK", "🔄 Using convenience method for checkout")
        Log.d("NovacSDK", "👤 Building customer data from individual parameters")

        val checkoutCustomerData = CheckoutCustomerData(
            email = customerEmail,
            firstName = customerFirstName,
            lastName = customerLastName,
            phoneNumber = customerPhone
        )

        Log.d("NovacSDK", "✅ Customer data built:")
        Log.d("NovacSDK", "  - Email: $customerEmail")
        Log.d("NovacSDK", "  - First Name: ${customerFirstName ?: "N/A"}")
        Log.d("NovacSDK", "  - Last Name: ${customerLastName ?: "N/A"}")
        Log.d("NovacSDK", "  - Phone: ${customerPhone ?: "N/A"}")

        return initiateCheckout(
            transactionReference = transactionReference,
            amount = amount,
            currency = currency,
            checkoutCustomerData = checkoutCustomerData, // Updated parameter name
            checkoutCustomizationData = null // Updated parameter name
        )
    }

    /**
     * Launch WebView with checkout URL - This is the main method apps should call
     */
    fun launchCheckoutFlow(
        context: Context,
        transactionReference: String,
        amount: Double,
        currency: String,
        customerEmail: String,
        customerFirstName: String? = null,
        customerLastName: String? = null,
        customerPhone: String? = null,
        customizationData: CheckoutCustomizationData? = null
    ) {
        val intent = Intent(context, NovacCheckoutActivity::class.java).apply {
            putExtra("TRANSACTION_REFERENCE", transactionReference)
            putExtra("AMOUNT", amount)
            putExtra("CURRENCY", currency)
            putExtra("CUSTOMER_EMAIL", customerEmail)
            putExtra("CUSTOMER_FIRST_NAME", customerFirstName)
            putExtra("CUSTOMER_LAST_NAME", customerLastName)
            putExtra("CUSTOMER_PHONE", customerPhone)

            // Pass customization data as bundle
            customizationData?.let { data ->
                val bundle = Bundle().apply {
                    putString("LOGO_URL", data.logoUrl)
                    putString("PAYMENT_DESCRIPTION", data.paymentDescription)
                    putString("MODAL_TITLE", data.checkoutModalTitle)
                }
                putExtra("CUSTOMIZATION_DATA", bundle)
            }

            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }

    /**
     * Get current SDK configuration
     */
    fun getConfig(): CheckoutConfig {
        Log.d("NovacSDK", "🔧 Retrieving SDK configuration")
        return config
    }

    /**
     * Validate SDK configuration
     */
    fun validateConfig(): Boolean {
        Log.d("NovacSDK", "🔍 Validating SDK configuration...")
        return try {
            check(::config.isInitialized)
            check(config.merchantId.isNotBlank())
            check(config.baseUrl.isNotBlank())
            check(config.apiKey.isNotBlank())
            Log.d("NovacSDK", "✅ SDK configuration is valid")
            true
        } catch (e: Exception) {
            Log.e("NovacSDK", "❌ SDK configuration validation failed:", e)
            false
        }
    }

    /**
     * Get SDK version information
     */
    fun getVersionInfo(): String {
        Log.d("NovacSDK", "📋 Retrieving SDK version info")
        return "NovacCheckout SDK v1.0.0"
    }
}