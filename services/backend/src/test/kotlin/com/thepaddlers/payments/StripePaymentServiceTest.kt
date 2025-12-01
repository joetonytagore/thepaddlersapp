package com.thepaddlers.payments

import com.stripe.model.PaymentIntent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class StripePaymentServiceTest {
    private val stripeKey = "sk_test_123"
    private val service = StripePaymentService(stripeKey)

    @Test
    fun `createPaymentIntent returns clientSecret and id`() {
        val mockIntent = mock<PaymentIntent>()
        whenever(mockIntent.clientSecret).thenReturn("secret_abc")
        whenever(mockIntent.id).thenReturn("pi_123")
        // Simulate Stripe client
        // You would use a test Stripe key and/or mock PaymentIntent.create
        val result = service.createPaymentIntent(1000, "usd", mapOf("reservationId" to "res1"), "idem-key")
        assertNotNull(result)
        assertEquals("secret_abc", result.clientSecret)
        assertEquals("pi_123", result.id)
    }
}

