# Novac Payment Android SDK

[![JitPack](https://jitpack.io/v/novacpayment/novac-payment-android-sdk.svg)](https://jitpack.io/#novacpayment/novac-payment-android-sdk)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Min SDK](https://img.shields.io/badge/min%20SDK-21+-green.svg)](https://developer.android.com/about/versions/android-5.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8+-blue.svg)](https://kotlinlang.org)

A modern Android SDK for integrating Novac Payment checkout functionality into your Android applications. This SDK provides a seamless payment experience with customizable UI and robust error handling.

## ✨ Features

- 🚀 **Easy Integration** - Simple setup and initialization
- 🔒 **Secure** - Built with security best practices
- 📱 **Native Android** - Fully compatible with Android Jetpack Compose and traditional Views
- 📊 **Comprehensive Logging** - Detailed logging for debugging

## 📦 Installation

### Add JitPack Repository

Add the JitPack repository to your project-level `build.gradle` file:

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
# Add Dependency

Add the dependency to your app-level `build.gradle` file:

```gradle
dependencies {
    implementation 'com.github.novacpayment:novac-payment-android-sdk:1.0.0'
}
```
## 🚀 Quick Start

### 1. Initialize the SDK

Initialize the SDK in your `Application` class or main `Activity`:

```kotlin
import com.novacpaymen.paywithnovac_android_skd.NovacCheckout
import com.novacpaymen.paywithnovac_android_skd.CheckoutConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        NovacCheckout.initialize(
            CheckoutConfig(
                merchantId = "your-merchant-id",
                baseUrl = "https://api.novacpayment.com",
                apiKey = "your-api-key",
                enableLogging = true,
                successActivityClass = SuccessActivity::class.java,
                failureActivityClass = FailureActivity::class.java
            )
        )
    }
}
```
### 2. Create Result Activities

Create activities to handle payment results:

#### `SuccessActivity.kt`

```kotlin
class SuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)
        
        // Retrieve payment result data
        val transactionReference = intent.getStringExtra("transaction_reference")
        val amount = intent.getDoubleExtra("amount", 0.0)
        val currency = intent.getStringExtra("currency")
        val paymentStatus = intent.getStringExtra("payment_status")
        
        // Display success message with transaction details
        Log.d("NovacSDK", "✅ Payment Successful:")
        Log.d("NovacSDK", "  - Transaction Ref: $transactionReference")
        Log.d("NovacSDK", "  - Amount: $amount $currency")
        Log.d("NovacSDK", "  - Status: $paymentStatus")
        
        // Update UI with success data
        // Example: show success message, update order status, etc.
    }
}
```
#### `FailureActivity.kt`

```kotlin
class FailureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failure)
        
        // Retrieve payment failure data
        val transactionReference = intent.getStringExtra("transaction_reference")
        val amount = intent.getDoubleExtra("amount", 0.0)
        val currency = intent.getStringExtra("currency")
        val errorMessage = intent.getStringExtra("error_message")
        val errorCode = intent.getStringExtra("error_code")
        
        // Display failure details
        Log.e("NovacSDK", "❌ Payment Failed:")
        Log.e("NovacSDK", "  - Transaction Ref: $transactionReference")
        Log.e("NovacSDK", "  - Amount: $amount $currency")
        Log.e("NovacSDK", "  - Error: $errorMessage")
        Log.e("NovacSDK", "  - Code: $errorCode")
        
        // Update UI with error information
        // Example: show retry button, display error message to user, etc.
    }
}
```
#### `FailureActivity.kt`

```kotlin
class FailureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failure)
        
        // Retrieve payment failure data
        val transactionReference = intent.getStringExtra("transaction_reference")
        val amount = intent.getDoubleExtra("amount", 0.0)
        val currency = intent.getStringExtra("currency")
        val errorMessage = intent.getStringExtra("error_message")
        val errorCode = intent.getStringExtra("error_code")
        
        // Display failure details
        Log.e("NovacSDK", "❌ Payment Failed:")
        Log.e("NovacSDK", "  - Transaction Ref: $transactionReference")
        Log.e("NovacSDK", "  - Amount: $amount $currency")
        Log.e("NovacSDK", "  - Error: $errorMessage")
        Log.e("NovacSDK", "  - Code: $errorCode")
        
        // Update UI with error information
        // Example: show retry button, display error message to user, etc.
    }
}
```
### 3. Launch Checkout Flow

Use the simple method to launch checkout:

```kotlin
val sdk = NovacCheckout.getInstance()

sdk.launchCheckoutFlow(
    context = this,
    transactionReference = UUID.randomUUID().toString(),
    amount = 120.0,
    currency = "NGN",
    customerEmail = "customer@example.com",
    customerFirstName = "John",
    customerLastName = "Doe",
    customerPhone = "1234567890"
)
```
## 🔧 Advanced Usage

### With Customization

Customize the checkout appearance:

```kotlin
val customizationData = CheckoutCustomizationData(
    logoUrl = "https://your-logo-url.com/logo.png",
    paymentDescription = "Payment for Order #123",
    checkoutModalTitle = "Complete Your Payment"
)

sdk.launchCheckoutFlow(
    context = this,
    transactionReference = UUID.randomUUID().toString(),
    amount = 120.0,
    currency = "NGN",
    customerEmail = "customer@example.com",
    customerFirstName = "John",
    customerLastName = "Doe",
    customerPhone = "1234567890",
    customizationData = customizationData
)
```
### Manual Checkout Initiation

For more control, use the manual approach:

```kotlin
val sdk = NovacCheckout.getInstance()

// Create customer data
val customerData = CheckoutCustomerData(
    email = "customer@example.com",
    firstName = "John",
    lastName = "Doe",
    phoneNumber = "1234567890"
)

// Create customization data
val customizationData = CheckoutCustomizationData(
    logoUrl = "https://your-logo-url.com/logo.png",
    paymentDescription = "Payment for Order #123",
    checkoutModalTitle = "Complete Your Payment"
)

// Initiate checkout manually
coroutineScope.launch {
    when (val result = sdk.initiateCheckout(
        transactionReference = UUID.randomUUID().toString(),
        amount = 120.0,
        currency = "NGN",
        checkoutCustomerData = customerData,
        checkoutCustomizationData = customizationData
    )) {
        is Result.Success -> {
            val response = result.value
            val paymentUrl = response.data?.paymentRedirectUrl
            // Handle success - redirect to payment URL
        }
        is Result.Failure -> {
            // Handle error
            Log.e("Checkout", "Payment initiation failed", result.exception)
        }
    }
}
```
## ⚙️ Configuration

### CheckoutConfig Parameters

| Parameter      | Type    | Required | Description                                      |
|----------------|---------|-----------|--------------------------------------------------|
| `merchantId`   | String  | ✅        | Your merchant ID from Novac                     |
| `baseUrl`      | String  | ✅        | API base URL (use `https://api.novacpayment.com`) |
| `apiKey`       | String  | ✅        | Your API key from Novac                         |
| `enableLogging`| Boolean | ❌        | Enable debug logging (default: false)           |

