package com.novacpaymen.paywithnovac_android_skd

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.novacpaymen.paywithnovac_android_skd.databinding.ActivityNovacWebviewBinding
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class NovacWebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNovacWebviewBinding
    private val handler = Handler(Looper.getMainLooper())
    private var pollingJob: Job? = null
    private var transactionReference: String? = null
    private var apiKey: String? = null
    private var isPollingActive = false
    private var pollingAttempts = 0
    private val maxPollingAttempts = 200 // 5 minutes (5 seconds * 60)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovacWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val checkoutUrl = intent.getStringExtra("CHECKOUT_URL") ?: run {
            Log.e("NovacSDK", "❌ No checkout URL provided")
            finish()
            return
        }

        apiKey = intent.getStringExtra("API_KEY")

        Log.d("NovacSDK", "🌐 Loading checkout URL: $checkoutUrl")
        Log.d("NovacSDK", "🔑 API Key: ${apiKey?.take(8)}...")

        // Extract transaction reference from checkout URL
        transactionReference = extractTransactionReference(checkoutUrl)
        Log.d("NovacSDK", "📝 Initial transaction reference: $transactionReference")

        // Log the configured activities for debugging
        val config = NovacCheckout.getInstance().getConfig()
        Log.d("NovacSDK", "🎯 Configured Success Activity: ${config.successActivityClass?.name ?: "None"}")
        Log.d("NovacSDK", "🎯 Configured Failure Activity: ${config.failureActivityClass?.name ?: "None"}")

        with(binding.webView) {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url.toString()
                    Log.d("NovacSDK", "➡️ Navigating to: $url")

                    // Extract transaction reference from any URL that might contain it
                    val refFromUrl = extractTransactionReference(url)
                    if (!refFromUrl.isNullOrBlank()) {
                        transactionReference = refFromUrl
                        Log.d("NovacSDK", "📝 Updated transaction reference from URL: $transactionReference")
                    }

                    // Check for various success/failure indicators
                    when {
                        url.contains("status=success", ignoreCase = true) -> {
                            Log.d("NovacSDK", "🎯 Success URL detected")
                            val uri = Uri.parse(url)
                            val reference = uri.getQueryParameter("reference") ?: transactionReference
                            startPolling(reference, "URL success trigger")
                            return true
                        }
                        url.contains("status=failed", ignoreCase = true) ||
                                url.contains("status=error", ignoreCase = true) -> {
                            Log.d("NovacSDK", "🎯 Failure URL detected")
                            val uri = Uri.parse(url)
                            val reference = uri.getQueryParameter("reference") ?: transactionReference
                            startPolling(reference, "URL failure trigger")
                            return true
                        }
                        url.contains("/success", ignoreCase = true) ||
                                url.contains("/complete", ignoreCase = true) ||
                                url.contains("/verify", ignoreCase = true) -> {
                            Log.d("NovacSDK", "🎯 Success page detected")
                            transactionReference?.let { ref ->
                                startPolling(ref, "Success page trigger")
                            }
                            return false // Let WebView load the page
                        }
                        url.contains("/failed", ignoreCase = true) ||
                                url.contains("/error", ignoreCase = true) -> {
                            Log.d("NovacSDK", "🎯 Failure page detected")
                            transactionReference?.let { ref ->
                                startPolling(ref, "Failure page trigger")
                            }
                            return false // Let WebView load the page
                        }
                    }

                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("NovacSDK", "✅ Page finished loading: $url")

                    // Auto-start polling after 10 seconds if not already started
                    if (!isPollingActive && transactionReference != null) {
                        handler.postDelayed({
                            if (!isPollingActive) {
                                Log.d("NovacSDK", "⏰ Auto-starting polling after page load")
                                startPolling(transactionReference!!, "Auto-start after page load")
                            }
                        }, 10000) // 10 seconds delay
                    }
                }
            }

            loadUrl(checkoutUrl)
        }

        // Start polling automatically after 15 seconds as fallback
        handler.postDelayed({
            if (!isPollingActive && transactionReference != null) {
                Log.d("NovacSDK", "⏰ Fallback: Auto-starting polling after 15 seconds")
                startPolling(transactionReference!!, "Fallback auto-start")
            }
        }, 15000)
    }

    private fun extractTransactionReference(url: String): String? {
        return try {
            val uri = Uri.parse(url)

            // Try to extract from query parameters
            var reference = uri.getQueryParameter("reference") ?:
            uri.getQueryParameter("transactionReference") ?:
            uri.getQueryParameter("trxref") ?:
            uri.getQueryParameter("tx_ref") ?:
            uri.getQueryParameter("transaction_ref")

            // If not found in query params, try to extract from path
            if (reference.isNullOrBlank()) {
                val segments = uri.pathSegments
                // Look for UUID-like patterns in the path
                segments.forEach { segment ->
                    if (segment.matches(Regex("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"))) {
                        reference = segment
                        Log.d("NovacSDK", "🔍 Found UUID in path: $reference")
                        return@forEach
                    }
                }
            }

            // Also check for numeric IDs in path
            if (reference.isNullOrBlank()) {
                val segments = uri.pathSegments
                segments.forEach { segment ->
                    if (segment.matches(Regex("\\d+"))) {
                        reference = segment
                        Log.d("NovacSDK", "🔍 Found numeric ID in path: $reference")
                        return@forEach
                    }
                }
            }

            Log.d("NovacSDK", "🔍 Extracted reference: $reference")
            reference
        } catch (e: Exception) {
            Log.e("NovacSDK", "❌ Error extracting transaction reference: ${e.message}")
            null
        }
    }

    private fun startPolling(reference: String?, trigger: String) {
        if (reference.isNullOrBlank()) {
            Log.e("NovacSDK", "❌ Cannot start polling - no transaction reference")
            return
        }

        if (isPollingActive) {
            Log.d("NovacSDK", "ℹ️ Polling already active, ignoring new request")
            return
        }

        transactionReference = reference
        isPollingActive = true
        pollingAttempts = 0

        Log.d("NovacSDK", "🔄 Starting transaction status polling for: $reference")
        Log.d("NovacSDK", "🎯 Trigger: $trigger")

        // Show toast to user
        showVerificationToast()

        pollingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isPollingActive && pollingAttempts < maxPollingAttempts) {
                try {
                    pollingAttempts++
                    Log.d("NovacSDK", "📊 Polling attempt $pollingAttempts/$maxPollingAttempts")

                    val transactionData = verifyTransactionStatus(reference)
                    val status = transactionData?.get("status") as? String

                    Log.d("NovacSDK", "📋 Polling result - Status: '$status', Attempt: $pollingAttempts")

                    // Debug: Log the exact status value we're checking
                    Log.d("NovacSDK", "🔍 Checking status: '$status' (length: ${status?.length})")

                    // Handle null status explicitly
                    if (status == null) {
                        Log.w("NovacSDK", "⚠️  Status is null, transaction data: $transactionData")
                        // Continue polling if status is null
                        delay(5000)
                        continue
                    }

                    when {
                        status.equals("successful", ignoreCase = true) -> {
                            Log.d("NovacSDK", "✅ Transaction verified as SUCCESSFUL - redirecting...")
                            withContext(Dispatchers.Main) {
                                redirectToSuccess(reference, status, apiKey, transactionData)
                            }
                            return@launch
                        }
                        status.equals("failed", ignoreCase = true) ||
                                status.equals("failure", ignoreCase = true) -> {
                            Log.d("NovacSDK", "❌ Transaction verified as FAILED - redirecting...")
                            withContext(Dispatchers.Main) {
                                redirectToFailure(reference, status, apiKey, transactionData)
                            }
                            return@launch
                        }
                        status.equals("pending", ignoreCase = true) -> {
                            Log.d("NovacSDK", "⏳ Transaction still PENDING...")
                            // Continue polling
                        }
                        else -> {
                            Log.w("NovacSDK", "❓ Unknown transaction status: '$status'")
                            // Continue polling for unknown status
                        }
                    }

                    // Wait 5 seconds before next poll
                    delay(5000)

                } catch (e: Exception) {
                    Log.e("NovacSDK", "❌ Error during polling attempt $pollingAttempts: ${e.message}")
                    // Wait 5 seconds before retry even on error
                    delay(5000)
                }
            }

            // If we reach here, polling ended without success/failure
            if (pollingAttempts >= maxPollingAttempts) {
                withContext(Dispatchers.Main) {
                    Log.w("NovacSDK", "⏰ Polling timeout after $pollingAttempts attempts")
                    showTimeoutToast()
                    // Optionally redirect to failure after timeout
                    redirectToFailure(reference, "timeout", apiKey, null)
                }
            }

            isPollingActive = false
            Log.d("NovacSDK", "🏁 Polling ended after $pollingAttempts attempts")
        }
    }

    private suspend fun verifyTransactionStatus(transactionRef: String): Map<String, Any>? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.novacpayment.com/api/v1/checkout/$transactionRef/verify"
                Log.d("NovacSDK", "🔍 Verifying transaction: $url")

                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d("NovacSDK", "📄 Verification response code: ${response.code}")
                Log.d("NovacSDK", "📄 Verification response body: $responseBody")

                // Log complete response for debugging
                logCompleteResponse(responseBody)

                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    val jsonResponse = JSONObject(responseBody)

                    // Better error handling for JSON parsing
                    val apiStatus = jsonResponse.optBoolean("status", false)
                    val message = jsonResponse.optString("message", "No message")

                    Log.d("NovacSDK", "📋 API Status: $apiStatus, Message: $message")

                    if (apiStatus && jsonResponse.has("data")) {
                        val data = jsonResponse.getJSONObject("data")
                        val transactionStatus = data.optString("status", "unknown")

                        Log.d("NovacSDK", "🎯 Transaction Status: '$transactionStatus'")

                        // Convert JSON object to map for easy serialization
                        val transactionData = mutableMapOf<String, Any>()
                        transactionData["status"] = transactionStatus

                        // Safely extract values with defaults
                        try {
                            transactionData["id"] = data.optInt("id", 0)
                            transactionData["transactionReference"] = data.optString("transactionReference", transactionRef)
                            transactionData["amount"] = data.optDouble("amount", 0.0)
                            transactionData["chargedAmount"] = data.optDouble("chargedAmount", 0.0)
                            transactionData["currency"] = data.optString("currency", "NGN")
                            transactionData["transactionFee"] = data.optDouble("transactionFee", 0.0)
                            transactionData["gatewayResponseCode"] = data.optString("gatewayResponseCode", "")
                            transactionData["gatewayResponseMessage"] = data.optString("gatewayResponseMessage", "")
                            transactionData["domain"] = data.optString("domain", "")
                            transactionData["channel"] = data.optString("channel", "")
                            transactionData["requestIp"] = data.optString("requestIp", "")
                            transactionData["paymentDescriptor"] = data.optString("paymentDescriptor", "")
                            transactionData["redirectUrl"] = data.optString("redirectUrl", "")

                            // Add additional fields that might be useful
                            transactionData["api_message"] = message
                            transactionData["api_status"] = apiStatus

                            // Add customer details if available
                            if (data.has("customer") && !data.isNull("customer")) {
                                val customer = data.getJSONObject("customer")
                                val customerDetails = mapOf(
                                    "id" to customer.optInt("id", 0),
                                    "customerCode" to customer.optString("customerCode", ""),
                                    "email" to customer.optString("email", ""),
                                    "name" to customer.optString("name", "")
                                )
                                transactionData["customer"] = customerDetails
                            }

                            // Add card details if available
                            if (data.has("card") && !data.isNull("card")) {
                                val card = data.getJSONObject("card")
                                val cardDetails = mapOf(
                                    "first6Digits" to card.optString("first6Digits", ""),
                                    "last4Digits" to card.optString("last4Digits", ""),
                                    "issuer" to card.optString("issuer", ""),
                                    "country" to card.optString("country", ""),
                                    "type" to card.optString("type", "")
                                )
                                transactionData["card"] = cardDetails
                            }

                            // Add transfer details if available
                            if (data.has("transferDetail") && !data.isNull("transferDetail")) {
                                val transfer = data.getJSONObject("transferDetail")
                                val transferDetails = mapOf(
                                    "bankCode" to transfer.optString("bankCode", ""),
                                    "bankName" to transfer.optString("bankName", ""),
                                    "accountNumber" to transfer.optString("accountNumber", ""),
                                    "sessionId" to transfer.optString("sessionId", ""),
                                    "creditAccountName" to transfer.optString("creditAccountName", ""),
                                    "originatorName" to transfer.optString("originatorName", ""),
                                    "originatorAccountNumber" to transfer.optString("originatorAccountNumber", "")
                                )
                                transactionData["transferDetail"] = transferDetails
                            }

                            Log.d("NovacSDK", "✅ Successfully parsed transaction data with status: $transactionStatus")
                            return@withContext transactionData

                        } catch (e: Exception) {
                            Log.e("NovacSDK", "❌ Error parsing transaction data: ${e.message}")
                            // Even if some fields fail, return the basic data with status
                            return@withContext transactionData
                        }
                    } else {
                        Log.e("NovacSDK", "❌ API returned error or no data: $message")
                    }
                } else {
                    Log.e("NovacSDK", "❌ HTTP error: ${response.code}, Body: $responseBody")
                }
            } catch (e: Exception) {
                Log.e("NovacSDK", "❌ Network error verifying transaction: ${e.message}")
            }

            return@withContext null
        }
    }

    private fun logCompleteResponse(responseBody: String?) {
        Log.d("NovacSDK", "🔍 COMPLETE API RESPONSE:")
        Log.d("NovacSDK", "Response: $responseBody")

        if (!responseBody.isNullOrBlank()) {
            try {
                val json = JSONObject(responseBody)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    Log.d("NovacSDK", "Key: $key = ${json.get(key)}")
                }
            } catch (e: Exception) {
                Log.e("NovacSDK", "Error parsing response for debugging: ${e.message}")
            }
        }
    }

    private fun showVerificationToast() {
        runOnUiThread {
            Toast.makeText(
                this,
                "⏳ You will be redirected shortly...",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showTimeoutToast() {
        runOnUiThread {
            Toast.makeText(
                this,
                "⏰ Transaction verification timeout. Please check your payment status later.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun redirectToSuccess(reference: String?, status: String?, apiKey: String?, transactionData: Map<String, Any>?) {
        Log.d("NovacSDK", "🎉 REDIRECT TO SUCCESS ACTIVITY CALLED")
        Log.d("NovacSDK", "📝 Reference: $reference, Status: $status")

        stopPolling()

        val config = NovacCheckout.getInstance().getConfig()

        if (config.successActivityClass == null) {
            Log.e("NovacSDK", "❌ CRITICAL: No success activity configured! Check your CheckoutConfig")
            Toast.makeText(this, "Transaction successful but no success activity configured", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Log.d("NovacSDK", "🎯 Starting success activity: ${config.successActivityClass.name}")

        try {
            val successIntent = Intent(this, config.successActivityClass).apply {
                putExtra("response_payload", "Reference: $reference\nStatus: $status")
                putExtra("api_key", apiKey)
                // Pass the complete transaction data as JSON string
                transactionData?.let { data ->
                    val jsonString = JSONObject(data).toString()
                    putExtra("transaction_data", jsonString)
                    putExtra("transaction_reference", reference)
                    Log.d("NovacSDK", "📦 Passing transaction data to success activity")
                }
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            startActivity(successIntent)
            Log.d("NovacSDK", "🚀 SUCCESS ACTIVITY STARTED SUCCESSFULLY!")
            finish()

        } catch (e: Exception) {
            Log.e("NovacSDK", "❌ FAILED to start success activity: ${e.message}", e)
            Toast.makeText(this, "Transaction successful but failed to redirect: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun redirectToFailure(reference: String?, status: String?, apiKey: String?, transactionData: Map<String, Any>?) {
        Log.d("NovacSDK", "💔 REDIRECT TO FAILURE ACTIVITY CALLED")
        Log.d("NovacSDK", "📝 Reference: $reference, Status: $status")

        stopPolling()

        val config = NovacCheckout.getInstance().getConfig()

        if (config.failureActivityClass == null) {
            Log.e("NovacSDK", "❌ CRITICAL: No failure activity configured! Check your CheckoutConfig")
            Toast.makeText(this, "Transaction failed but no failure activity configured", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Log.d("NovacSDK", "🎯 Starting failure activity: ${config.failureActivityClass.name}")

        try {
            val failureIntent = Intent(this, config.failureActivityClass).apply {
                putExtra("response_payload", "Reference: $reference\nStatus: $status")
                putExtra("api_key", apiKey)
                // Pass the complete transaction data as JSON string
                transactionData?.let { data ->
                    val jsonString = JSONObject(data).toString()
                    putExtra("transaction_data", jsonString)
                    putExtra("transaction_reference", reference)
                    Log.d("NovacSDK", "📦 Passing transaction data to failure activity")
                }
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            startActivity(failureIntent)
            Log.d("NovacSDK", "🚀 FAILURE ACTIVITY STARTED SUCCESSFULLY!")
            finish()

        } catch (e: Exception) {
            Log.e("NovacSDK", "❌ FAILED to start failure activity: ${e.message}", e)
            Toast.makeText(this, "Transaction failed but failed to redirect: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun stopPolling() {
        isPollingActive = false
        pollingJob?.cancel()
        handler.removeCallbacksAndMessages(null)
        Log.d("NovacSDK", "🛑 Stopped transaction polling")
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            stopPolling()
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
        binding.webView.stopLoading()
        binding.webView.destroy()
        Log.d("NovacSDK", "🔚 NovacWebViewActivity destroyed")
    }
}