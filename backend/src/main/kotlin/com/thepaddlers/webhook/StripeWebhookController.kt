package com.thepaddlers.webhook

import com.stripe.exception.SignatureVerificationException
import com.stripe.model.Event
import com.stripe.net.Webhook
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import org.thepaddlers.repository.StripeEventRepository
import org.thepaddlers.model.StripeEvent
import org.springframework.beans.factory.annotation.Autowired
import org.thepaddlers.repository.InvoiceRepository
import org.thepaddlers.repository.PaymentTransactionRepository

@RestController
class StripeWebhookController(
    private val stripeService: StripeEventService // implement to persist payments/invoices and idempotency
) {
    private val endpointSecret: String = System.getenv("STRIPE_WEBHOOK_SECRET") ?: ""

    @PostMapping("/webhooks/stripe")
    @Transactional
    fun handle(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") sigHeader: String?
    ): ResponseEntity<String> {
        if (sigHeader == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing signature")

        val event: Event
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret)
        } catch (ex: SignatureVerificationException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature")
        } catch (ex: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload")
        }

        // Idempotency: ensure we haven't processed this event before
        if (stripeService.isAlreadyProcessed(event.id)) {
            return ResponseEntity.ok("Already processed")
        }

        when (event.type) {
            "invoice.payment_succeeded" -> {
                stripeService.handleInvoicePaymentSucceeded(event)
            }
            "invoice.payment_failed" -> {
                stripeService.handleInvoicePaymentFailed(event)
            }
            "charge.refunded" -> {
                stripeService.handleChargeRefunded(event)
            }
            else -> {
                // ignore unhandled events or log
            }
        }

        stripeService.markProcessed(event.id)
        return ResponseEntity.ok("Processed")
    }
}

interface StripeEventService {
    fun isAlreadyProcessed(eventId: String): Boolean
    fun markProcessed(eventId: String)
    fun handleInvoicePaymentSucceeded(event: com.stripe.model.Event)
    fun handleInvoicePaymentFailed(event: com.stripe.model.Event)
    fun handleChargeRefunded(event: com.stripe.model.Event)
}

@Service
class StripeEventServiceImpl @Autowired constructor(
    private val stripeEventRepository: StripeEventRepository,
    private val invoiceRepository: InvoiceRepository,
    private val paymentTransactionRepository: PaymentTransactionRepository
) : StripeEventService {
    override fun isAlreadyProcessed(eventId: String): Boolean {
        return stripeEventRepository.existsById(eventId)
    }

    override fun markProcessed(eventId: String) {
        if (!stripeEventRepository.existsById(eventId)) {
            stripeEventRepository.save(StripeEvent(eventId, ""))
        }
    }

    override fun handleInvoicePaymentSucceeded(event: com.stripe.model.Event) {
        // TODO: parse event.data.object and update Invoice/payment status
        // Example: mark invoice as PAID
        // val invoiceId = ... // extract from event
        // val invoice = invoiceRepository.findById(invoiceId)
        // invoice.status = "PAID"
        // invoiceRepository.save(invoice)
    }

    override fun handleInvoicePaymentFailed(event: com.stripe.model.Event) {
        // TODO: parse event.data.object and update Invoice/payment status
        // Example: mark invoice as FAILED
    }

    override fun handleChargeRefunded(event: com.stripe.model.Event) {
        // TODO: parse event.data.object and update PaymentTransaction status
        // Example: mark payment as REFUNDED
    }
}
