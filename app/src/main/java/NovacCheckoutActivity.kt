package com.novacpaymen.paywithnovac_android_skd

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.novacpaymen.paywithnovac_android_skd.models.CheckoutCustomizationData
import com.novacpaymen.paywithnovac_android_skd.models.CheckoutCustomerData
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch

class NovacCheckoutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("NovacSDK", "🎬 NovacCheckoutActivity started")

        setContent {
            NovacCheckoutFlow()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovacCheckoutFlow() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Get intent extras
    val transactionReference = remember {
        (context as? NovacCheckoutActivity)?.intent?.getStringExtra("TRANSACTION_REFERENCE") ?: ""
    }
    val amount = remember {
        (context as? NovacCheckoutActivity)?.intent?.getDoubleExtra("AMOUNT", 0.0) ?: 0.0
    }
    val currency = remember {
        (context as? NovacCheckoutActivity)?.intent?.getStringExtra("CURRENCY") ?: "USD"
    }
    val customerEmail = remember {
        (context as? NovacCheckoutActivity)?.intent?.getStringExtra("CUSTOMER_EMAIL") ?: ""
    }
    val customerFirstName = remember {
        (context as? NovacCheckoutActivity)?.intent?.getStringExtra("CUSTOMER_FIRST_NAME")
    }
    val customerLastName = remember {
        (context as? NovacCheckoutActivity)?.intent?.getStringExtra("CUSTOMER_LAST_NAME")
    }
    val customerPhone = remember {
        (context as? NovacCheckoutActivity)?.intent?.getStringExtra("CUSTOMER_PHONE")
    }

    // Get customization data from intent
    val customizationData = remember {
        val customizationBundle = (context as? NovacCheckoutActivity)?.intent?.getBundleExtra("CUSTOMIZATION_DATA")
        if (customizationBundle != null) {
            CheckoutCustomizationData(
                logoUrl = customizationBundle.getString("LOGO_URL"),
                paymentDescription = customizationBundle.getString("PAYMENT_DESCRIPTION"),
                checkoutModalTitle = customizationBundle.getString("MODAL_TITLE")
            )
        } else {
            null
        }
    }

    // Function to launch WebView activity
    val launchWebViewActivity = { checkoutUrl: String ->
        Log.d("NovacSDK", "🚀 Launching WebView activity with URL: $checkoutUrl")

        val intent = Intent(context, NovacWebViewActivity::class.java).apply {
            putExtra("CHECKOUT_URL", checkoutUrl)
            putExtra("API_KEY", NovacCheckout.getInstance().getConfig().apiKey)
        }

        context.startActivity(intent)
        (context as? NovacCheckoutActivity)?.finish()
    }

    LaunchedEffect(Unit) {
        Log.d("NovacSDK", "🔄 Starting checkout process in activity")

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

        coroutineScope.launch {
            try {
                val sdk = NovacCheckout.getInstance()

                // Build customer data object
                val customerData = CheckoutCustomerData(
                    email = customerEmail,
                    firstName = customerFirstName,
                    lastName = customerLastName,
                    phoneNumber = customerPhone
                )

                // Create a mock request object to log the JSON structure
                val payload = mapOf(
                    "transactionReference" to transactionReference,
                    "amount" to amount,
                    "currency" to currency,
                    "checkoutCustomerData" to mapOf(
                        "email" to customerData.email,
                        "firstName" to customerData.firstName,
                        "lastName" to customerData.lastName,
                        "phoneNumber" to customerData.phoneNumber
                    ),
                    "checkoutCustomizationData" to if (customizationData != null) {
                        mapOf(
                            "logoUrl" to customizationData.logoUrl,
                            "paymentDescription" to customizationData.paymentDescription,
                            "checkoutModalTitle" to customizationData.checkoutModalTitle
                        )
                    } else null
                )

                // Pretty print the JSON payload
                val gson = GsonBuilder().setPrettyPrinting().create()
                val jsonPayload = gson.toJson(payload)

                Log.d("NovacSDK", "📤 COMPLETE PAYLOAD STRUCTURE:")
                Log.d("NovacSDK", "=== JSON PAYLOAD START ===")
                Log.d("NovacSDK", jsonPayload)
                Log.d("NovacSDK", "=== JSON PAYLOAD END ===")

                Log.d("NovacSDK", "📦 Making API call with parameters:")
                Log.d("NovacSDK", "  - Transaction Reference: $transactionReference")
                Log.d("NovacSDK", "  - Amount: $amount")
                Log.d("NovacSDK", "  - Currency: $currency")
                Log.d("NovacSDK", "  - Customer Email: $customerEmail")
                Log.d("NovacSDK", "  - Customization Data Present: ${customizationData != null}")

                // Make the actual API call using the correct method signature
                val response = sdk.initiateCheckout(
                    transactionReference = transactionReference,
                    amount = amount,
                    currency = currency,
                    checkoutCustomerData = customerData,  // Use CheckoutCustomerData object
                    checkoutCustomizationData = customizationData  // Use CheckoutCustomizationData object
                )

                response.fold(
                    onSuccess = { successResponse ->
                        if (successResponse.status && successResponse.data != null) {
                            val checkoutUrl = successResponse.data.paymentRedirectUrl
                            Log.d("NovacSDK", "✅ Checkout URL received: $checkoutUrl")

                            // Launch the WebView activity with the checkout URL
                            launchWebViewActivity(checkoutUrl)
                        } else {
                            errorMessage = successResponse.message ?: "Failed to initiate checkout"
                            Log.e("NovacSDK", "❌ API error: $errorMessage")
                            isLoading = false
                        }
                    },
                    onFailure = { error ->
                        errorMessage = error.message ?: "Unknown error occurred"
                        Log.e("NovacSDK", "❌ Checkout failed: $errorMessage", error)
                        isLoading = false
                    }
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error occurred"
                Log.e("NovacSDK", "❌ Exception in checkout flow: $errorMessage", e)
                isLoading = false
            }
        }
    }

    Scaffold() { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    // Loading state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Preparing your payment...")
                    }
                }
                else -> {
                    // Error state (only show if there's an error and no WebView was launched)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "❌ Payment Failed",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "Unknown error occurred",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { (context as? NovacCheckoutActivity)?.finish() }
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}