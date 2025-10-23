package com.novacpaymen.demo_app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.novacpaymen.paywithnovac_android_skd.NovacCheckout
import com.novacpaymen.paywithnovac_android_skd.CheckoutConfig
import com.novacpaymen.paywithnovac_android_skd.models.CheckoutCustomizationData
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the SDK with demo credentials using CheckoutConfig
        NovacCheckout.initialize(
            CheckoutConfig(
                merchantId = "1000000397",
                baseUrl = "https://api.novacpayment.com",
                apiKey = "nc_testpk_bzq7y159l008q69s9pmraholmemp063rzbax",
                enableLogging = true,
                successActivityClass = NovacSuccessActivity::class.java, // Add this
                failureActivityClass = NovacFailedActivity::class.java   // Add this
            )
        )

        // Log SDK initialization
        Log.d("NovacSDK", "SDK Initialized with:")
        Log.d("NovacSDK", "  Merchant ID: 1000000397")
        Log.d("NovacSDK", "  Base URL: https://api.novacpayment.com")
        Log.d("NovacSDK", "  API Key: nc_testpk_bzq7y159l008q69s9pmraholmemp063rzbax")
        Log.d("NovacSDK", "  Success Activity: ${NovacSuccessActivity::class.java.name}")
        Log.d("NovacSDK", "  Failure Activity: ${NovacFailedActivity::class.java.name}")

        setContent {
            DemoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CheckoutInputForm()
                }
            }
        }
    }
}

@Composable
fun DemoAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutInputForm() {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Form state - Using exact values from working curl example
    var transactionRef by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var amount by remember { mutableStateOf("120") } // Exactly 120 like in curl
    var currency by remember { mutableStateOf("NGN") } // Exactly NGN like in curl
    var customerEmail by remember { mutableStateOf("Judy25@yahoo.com") } // Exactly like curl
    var firstName by remember { mutableStateOf("Damian") } // Exactly like curl
    var lastName by remember { mutableStateOf("Reichert") } // Exactly like curl
    var phoneNumber by remember { mutableStateOf("12345678901") } // Exactly like curl

    // Customization fields - Updated to match curl example exactly
    var logoUrl by remember { mutableStateOf("https://cloudflare-ipfs.com/ipfs/Qmd3W5DuhgHirLHGVixi6V76LhCkZUz6pnFt5AJBiyvHye/avatar/962.jpg") }
    var paymentDescription by remember { mutableStateOf("A Checkout demonstration.") }
    var modalTitle by remember { mutableStateOf("Test Payments") }

    // Result state
    var result by remember { mutableStateOf("Fill the form and click 'Initiate Checkout'") }
    var isLoading by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Novac Checkout SDK Demo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Enter payment details to test the checkout flow",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Transaction Details Section
        Text(
            text = "Transaction Details",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = transactionRef,
            onValueChange = { transactionRef = it },
            label = { Text("Transaction Reference *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = true,
            trailingIcon = {
                IconButton(
                    onClick = {
                        transactionRef = UUID.randomUUID().toString()
                    }
                ) {
                    Text("🔄")
                }
            }
        )

        Text(
            text = "This is automatically generated. Click the refresh icon to generate a new one.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount *") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                label = { Text("Currency *") },
                modifier = Modifier.weight(0.3f),
                singleLine = true
            )
        }

        // Customer Details Section
        Text(
            text = "Customer Information",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = customerEmail,
            onValueChange = { customerEmail = it },
            label = { Text("Email *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        // Customization Section (Optional) - Updated field names
        Text(
            text = "Customization (Optional)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = logoUrl,
            onValueChange = { logoUrl = it },
            label = { Text("Logo URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = paymentDescription,
            onValueChange = { paymentDescription = it },
            label = { Text("Payment Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = modalTitle,
            onValueChange = { modalTitle = it },
            label = { Text("Checkout Modal Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Action Button - Launch Checkout Flow
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isLoading) return@Button

                // Basic validation
                if (transactionRef.isBlank() || amount.isBlank() || currency.isBlank() || customerEmail.isBlank()) {
                    result = "❌ Please fill all required fields (*)"
                    showResult = true
                    return@Button
                }

                // Validate UUID format
                if (!isValidUUID(transactionRef)) {
                    result = "❌ Invalid transaction reference format. Please use a valid UUID."
                    showResult = true
                    return@Button
                }

                val amountValue = amount.toDoubleOrNull()
                if (amountValue == null || amountValue <= 0) {
                    result = "❌ Please enter a valid amount"
                    showResult = true
                    return@Button
                }

                isLoading = true
                result = "Launching checkout flow with form values..."
                showResult = true

                // Log the request details
                Log.d("NovacSDK", "=== CHECKOUT REQUEST ===")
                Log.d("NovacSDK", "Transaction Reference: $transactionRef")
                Log.d("NovacSDK", "Amount: $amountValue")
                Log.d("NovacSDK", "Currency: $currency")
                Log.d("NovacSDK", "Customer Email: $customerEmail")
                Log.d("NovacSDK", "First Name: $firstName")
                Log.d("NovacSDK", "Last Name: $lastName")
                Log.d("NovacSDK", "Phone: $phoneNumber")
                Log.d("NovacSDK", "Logo URL: $logoUrl")
                Log.d("NovacSDK", "Payment Description: $paymentDescription")
                Log.d("NovacSDK", "Modal Title: $modalTitle")
                Log.d("NovacSDK", "=== END REQUEST ===")

                // Direct launch without coroutine since launchCheckoutFlow is not a suspend function
                try {
                    val sdk = NovacCheckout.getInstance()

                    Log.d("NovacSDK", "🔄 Calling SDK launchCheckoutFlow...")

                    // Create customization data if any field is filled
                    val customizationData = if (logoUrl.isNotBlank() || paymentDescription.isNotBlank() || modalTitle.isNotBlank()) {
                        CheckoutCustomizationData(
                            logoUrl = if (logoUrl.isNotBlank()) logoUrl else null,
                            paymentDescription = if (paymentDescription.isNotBlank()) paymentDescription else null,
                            checkoutModalTitle = if (modalTitle.isNotBlank()) modalTitle else null
                        ).also {
                            Log.d("NovacSDK", "✅ Customization data created:")
                            Log.d("NovacSDK", "  - Logo URL: ${it.logoUrl}")
                            Log.d("NovacSDK", "  - Payment Description: ${it.paymentDescription}")
                            Log.d("NovacSDK", "  - Modal Title: ${it.checkoutModalTitle}")
                        }
                    } else {
                        Log.d("NovacSDK", "ℹ️ No customization data provided")
                        null
                    }

                    sdk.launchCheckoutFlow(
                        context = context,
                        transactionReference = transactionRef,
                        amount = amountValue,
                        currency = currency,
                        customerEmail = customerEmail,
                        customerFirstName = firstName,
                        customerLastName = lastName,
                        customerPhone = phoneNumber,
                        customizationData = customizationData  // Make sure this is passed
                    )

                    Log.d("NovacSDK", "✅ Checkout flow launched successfully")
                    result = "✅ Checkout flow launched successfully!\n" +
                            "Transaction Ref: $transactionRef\n" +
                            "Amount: $amountValue $currency\n" +
                            "Check the WebView for payment processing."

                } catch (e: Exception) {
                    Log.e("NovacSDK", "❌ Exception during checkout:", e)
                    result = "❌ Exception: ${e.message ?: "Unknown error"}\n" +
                            "Please check logs for details."
                } finally {
                    isLoading = false
                    Log.d("NovacSDK", "Checkout process completed")
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launching Checkout...")
            } else {
                Text("Launch Checkout Flow")
            }
        }

        // Test with exact curl values button
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (isLoading) return@Button

                isLoading = true
                result = "Testing with exact curl example values..."
                showResult = true

                try {
                    val sdk = NovacCheckout.getInstance()

                    Log.d("NovacSDK", "🔄 Testing with exact curl values...")

                    // Use exact values from curl example including customization
                    val customizationData = CheckoutCustomizationData(
                        logoUrl = "https://cloudflare-ipfs.com/ipfs/Qmd3W5DuhgHirLHGVixi6V76LhCkZUz6pnFt5AJBiyvHye/avatar/962.jpg",
                        paymentDescription = "A Checkout demonstration.",
                        checkoutModalTitle = "Test Payments"
                    ).also {
                        Log.d("NovacSDK", "✅ Exact curl customization data:")
                        Log.d("NovacSDK", "  - Logo URL: ${it.logoUrl}")
                        Log.d("NovacSDK", "  - Payment Description: ${it.paymentDescription}")
                        Log.d("NovacSDK", "  - Modal Title: ${it.checkoutModalTitle}")
                    }

                    sdk.launchCheckoutFlow(
                        context = context,
                        transactionReference = "b1d35105-5aa4-485b-a7af-415a220cbf39",
                        amount = 120.0,
                        currency = "NGN",
                        customerEmail = "Judy25@yahoo.com",
                        customerFirstName = "Damian",
                        customerLastName = "Reichert",
                        customerPhone = "12345678901",
                        customizationData = customizationData  // Pass customization data
                    )

                    Log.d("NovacSDK", "✅ Exact curl values checkout flow launched")
                    result = "✅ Exact curl values checkout flow launched!\n" +
                            "Using all values from the working curl example including customization."

                } catch (e: Exception) {
                    Log.e("NovacSDK", "❌ Exact curl test failed:", e)
                    result = "❌ Exact curl test failed: ${e.message ?: "Unknown error"}"
                } finally {
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Testing...")
            } else {
                Text("Test with Exact Curl Values")
            }
        }

        // Test without customization button
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (isLoading) return@Button

                isLoading = true
                result = "Testing without customization data..."
                showResult = true

                try {
                    val sdk = NovacCheckout.getInstance()

                    Log.d("NovacSDK", "🔄 Testing without customization data...")

                    sdk.launchCheckoutFlow(
                        context = context,
                        transactionReference = "b1d35105-5aa4-485b-a7af-415a220cbf39",
                        amount = 120.0,
                        currency = "NGN",
                        customerEmail = "Judy25@yahoo.com",
                        customerFirstName = "Damian",
                        customerLastName = "Reichert",
                        customerPhone = "12345678901",
                        customizationData = null  // Explicitly no customization
                    )

                    Log.d("NovacSDK", "✅ Checkout flow launched without customization")
                    result = "✅ Checkout flow launched without customization data!\n" +
                            "Using basic customer information only."

                } catch (e: Exception) {
                    Log.e("NovacSDK", "❌ Basic test failed:", e)
                    result = "❌ Basic test failed: ${e.message ?: "Unknown error"}"
                } finally {
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Testing...")
            } else {
                Text("Test Without Customization")
            }
        }

        // Generate new UUID button
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                transactionRef = UUID.randomUUID().toString()
                result = "🔄 New transaction reference generated: $transactionRef"
                showResult = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate New Transaction Reference")
        }

        // Info Text
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Note: The 'Test with Exact Curl Values' button uses the exact same data as the working curl example, including customization.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Result Section
        if (showResult) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = result,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Helper function to validate UUID format
 */
private fun isValidUUID(uuid: String): Boolean {
    return try {
        UUID.fromString(uuid)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}