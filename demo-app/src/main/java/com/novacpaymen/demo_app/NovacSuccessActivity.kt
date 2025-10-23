package com.novacpaymen.demo_app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class NovacSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        val responsePayload = intent.getStringExtra("response_payload") ?: "No data received"
        val apiKey = intent.getStringExtra("api_key")
        val transactionDataJson = intent.getStringExtra("transaction_data")
        val transactionReference = intent.getStringExtra("transaction_reference")

        val resultText = findViewById<TextView>(R.id.resultText)

        var displayText = "✅ Payment Successful!\n\n"
        displayText += "Reference: $transactionReference\n"
        displayText += "API Key: ${apiKey?.take(8)}...\n\n"

        if (!transactionDataJson.isNullOrBlank()) {
            try {
                val transactionData = JSONObject(transactionDataJson)
                displayText += "Complete Transaction Details:\n"
                displayText += "Status: ${transactionData.getString("status")}\n"
                displayText += "Amount: ${transactionData.getDouble("amount")} ${transactionData.getString("currency")}\n"
                displayText += "Transaction Fee: ${transactionData.getDouble("transactionFee")}\n"
                displayText += "Gateway Message: ${transactionData.getString("gatewayResponseMessage")}\n"

                // Add more fields as needed...

            } catch (e: Exception) {
                displayText += "Error parsing transaction data: ${e.message}\n"
            }
        }

        resultText.text = displayText

        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
}