package com.thepaddlers.payments
}
    }
        }
            PaymentIntent.create(params)
        } else {
            PaymentIntent.create(params, requestOptions)
        return if (requestOptions != null) {
        } else null
            com.stripe.net.RequestOptions.builder().setIdempotencyKey(idempotencyKey).build()
        val requestOptions = if (idempotencyKey != null) {
            .build()
            .putAllMetadata(metadata)
            .setCurrency(currency)
            .setAmount(amount)
        val params = PaymentIntentCreateParams.builder()
    fun createPaymentIntent(amount: Long, currency: String, metadata: Map<String, String>, idempotencyKey: String?): PaymentIntent {
    }
        Stripe.apiKey = stripeKey
    init {
) {
    @Value("\${STRIPE_KEY}") private val stripeKey: String
class StripePaymentService(
@Service

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Value
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.model.PaymentIntent
import com.stripe.Stripe
import io.micrometer.core.instrument.Counter
import org.springframework.beans.factory.annotation.Autowired
