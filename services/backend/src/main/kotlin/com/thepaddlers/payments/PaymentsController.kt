package com.thepaddlers.payments

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.beans.factory.annotation.Autowired

@RestController
@RequestMapping("/payments")
class PaymentsController @Autowired constructor(
    private val stripePaymentService: StripePaymentService
) {
    @PostMapping("/create-intent")
    fun createIntent(
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: String?,
        @RequestBody req: CreateIntentRequest
    ): ResponseEntity<CreateIntentResponse> {
        val paymentIntent = stripePaymentService.createPaymentIntent(
            req.amount,
            req.currency,
            mapOf("reservationId" to req.reservationId),
            idempotencyKey
        )
        return ResponseEntity.ok(
            CreateIntentResponse(
                clientSecret = paymentIntent.clientSecret,
                paymentIntentId = paymentIntent.id
            )
        )
    }
}

data class CreateIntentRequest(
    val amount: Long,
    val currency: String,
    val reservationId: String
)
data class CreateIntentResponse(
    val clientSecret: String?,
    val paymentIntentId: String?
)

