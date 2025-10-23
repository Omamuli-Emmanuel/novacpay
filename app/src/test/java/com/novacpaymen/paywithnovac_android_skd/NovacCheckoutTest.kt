package com.novacpaymen.paywithnovac_android_skd

import NovacCheckout
import CheckoutConfig
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NovacCheckoutTest {

    private lateinit var config: CheckoutConfig

    @Before
    fun setup() {
        config = CheckoutConfig(
            merchantId = "test_merchant",
            baseUrl = "https://api.test.com",
            apiKey = "test_api_key",
            enableLogging = false
        )
    }

    @Test
    fun `test SDK initialization`() {
        val sdk = NovacCheckout.initialize(config)
        assertNotNull(sdk)
        assertEquals(sdk, NovacCheckout.getInstance())
    }

    @Test(expected = IllegalStateException::class)
    fun `test getInstance without initialization throws exception`() {
        // Reset instance for this test
        val field = NovacCheckout::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)

        NovacCheckout.getInstance() // Should throw exception
    }

    @Test
    fun `test initiateCheckout creates correct request`() = runTest {
        NovacCheckout.initialize(config)
        val sdk = NovacCheckout.getInstance()

        // This will test the request building logic
        // We'll mock the network call later
    }
}