package com.novacpaymen.paywithnovac_android_skd

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.novacpaymen.paywithnovac_android_skd.models.CheckoutCustomerData
import com.novacpaymen.paywithnovac_android_skd.models.CheckoutCustomizationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NovacCheckoutActivity : AppCompatActivity() {

    private lateinit var loadingLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var errorMessage: TextView
    private lateinit var closeButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novac_checkout)

        Log.d("NovacSDK", "🎬 NovacCheckoutActivity started")

        // Initialize views
        initViews()

        // Start the checkout process
        startCheckoutProcess()
    }

    private fun initViews() {
        loadingLayout = findViewById(R.id.loadingLayout)
        errorLayout = findViewById(R.id.errorLayout)
        errorMessage = findViewById(R.id.errorMessage)
        closeButton = findViewById(R.id.closeButton)

        // Make sure this ID matches your XML layout file
        progressBar = findViewById(R.id.progressBar)

        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun startCheckoutProcess() {
        Log.d("NovacSDK", "🔄 Starting checkout process in activity")

        // Get intent extras
        val transactionReference = intent.getStringExtra("TRANSACTION_REFERENCE") ?: ""
        val amount = intent.getDoubleExtra("AMOUNT", 0.0)
        val currency = intent.getStringExtra("CURRENCY") ?: "USD"
        val customerEmail = intent.getStringExtra("CUSTOMER_EMAIL") ?: ""
        val customerFirstName = intent.getStringExtra("CUSTOMER_FIRST_NAME")
        val customerLastName = intent.getStringExtra("CUSTOMER_LAST_NAME")
        val customerPhone = intent.getStringExtra("CUSTOMER_PHONE")

        // Get customization data from intent - using direct string extras
        val logoUrl = intent.getStringExtra("LOGO_URL")
        val paymentDescription = intent.getStringExtra("PAYMENT_DESCRIPTION")
        val checkoutModalTitle = intent.getStringExtra("MODAL_TITLE")

        // Create CheckoutCustomizationData object instead of Map
        val customizationData = if (!logoUrl.isNullOrEmpty() || !paymentDescription.isNullOrEmpty() || !checkoutModalTitle.isNullOrEmpty()) {
            CheckoutCustomizationData(
                logoUrl = logoUrl,
                paymentDescription = paymentDescription,
                checkoutModalTitle = checkoutModalTitle
            )
        } else {
            null
        }

        // Log all received data
        Log.d("NovacSDK", "📥 RECEIVED INTENT DATA:")
        Log.d("NovacSDK", "  - Transaction Reference: $transactionReference")
        Log.d("NovacSDK", "  - Amount: $amount")
        Log.d("NovacSDK", "  - Currency: $currency")
        Log.d("NovacSDK", "  - Customer Email: $customerEmail")
        Log.d("NovacSDK", "  - Customer First Name: $customerFirstName")
        Log.d("NovacSDK", "  - Customer Last Name: $customerLastName")
        Log.d("NovacSDK", "  - Customer Phone: $customerPhone")
        Log.d("NovacSDK", "  - Customization Data: ${if (customizationData != null) "PRESENT" else "NULL"}")

        if (customizationData != null) {
            Log.d("NovacSDK", "  - Logo URL: ${customizationData.logoUrl}")
            Log.d("NovacSDK", "  - Payment Description: ${customizationData.paymentDescription}")
            Log.d("NovacSDK", "  - Modal Title: ${customizationData.checkoutModalTitle}")
        }

        // Start coroutine for API call
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val sdk = NovacCheckout.getInstance()

                // Build customer data object
                val customerData = CheckoutCustomerData(
                    email = customerEmail,
                    firstName = customerFirstName,
                    lastName = customerLastName,
                    phoneNumber = customerPhone
                )

                Log.d("NovacSDK", "📦 Making API call with parameters:")
                Log.d("NovacSDK", "  - Transaction Reference: $transactionReference")
                Log.d("NovacSDK", "  - Amount: $amount")
                Log.d("NovacSDK", "  - Currency: $currency")
                Log.d("NovacSDK", "  - Customer Email: $customerEmail")
                Log.d("NovacSDK", "  - Customization Data Present: ${customizationData != null}")

                // Make the actual API call
                val response = sdk.initiateCheckout(
                    transactionReference = transactionReference,
                    amount = amount,
                    currency = currency,
                    checkoutCustomerData = customerData,
                    checkoutCustomizationData = customizationData
                )

                response.fold(
                    onSuccess = { successResponse ->
                        if (successResponse.status && successResponse.data != null) {
                            val checkoutUrl = successResponse.data.paymentRedirectUrl
                            Log.d("NovacSDK", "✅ Checkout URL received: $checkoutUrl")

                            // Launch the WebView activity with the checkout URL
                            launchWebViewActivity(checkoutUrl)
                        } else {
                            val errorMsg = successResponse.message ?: "Failed to initiate checkout"
                            showError(errorMsg)
                            Log.e("NovacSDK", "❌ API error: $errorMsg")
                        }
                    },
                    onFailure = { error ->
                        val errorMsg = error.message ?: "Unknown error occurred"
                        showError(errorMsg)
                        Log.e("NovacSDK", "❌ Checkout failed: $errorMsg", error)
                    }
                )
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error occurred"
                showError(errorMsg)
                Log.e("NovacSDK", "❌ Exception in checkout flow: $errorMsg", e)
            }
        }
    }

    private fun launchWebViewActivity(checkoutUrl: String) {
        Log.d("NovacSDK", "🚀 Launching WebView activity with URL: $checkoutUrl")

        val intent = Intent(this, NovacWebViewActivity::class.java).apply {
            putExtra("CHECKOUT_URL", checkoutUrl)
            putExtra("API_KEY", NovacCheckout.getInstance().getConfig().apiKey)
        }

        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        runOnUiThread {
            loadingLayout.visibility = View.GONE
            errorLayout.visibility = View.VISIBLE
            errorMessage.text = message
        }
    }

    private fun showLoading() {
        runOnUiThread {
            loadingLayout.visibility = View.VISIBLE
            errorLayout.visibility = View.GONE
        }
    }
}