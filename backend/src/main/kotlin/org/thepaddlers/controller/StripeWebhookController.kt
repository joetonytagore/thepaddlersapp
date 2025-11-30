package org.thepaddlers.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.thepaddlers.model.StripeEvent
import org.thepaddlers.repository.StripeEventRepository
import org.springframework.transaction.annotation.Transactional
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/webhooks")
class StripeWebhookController(
    @Value("\${stripe.webhook.secret:}") private val webhookSecret: String?,
    private val stripeEventRepo: StripeEventRepository
) {
    private val objectMapper = ObjectMapper()

    @PostMapping("/stripe")
    @Transactional
    fun handleStripeWebhook(
        @RequestHeader("Stripe-Signature") sigHeader: String?,
        @RequestBody payload: Map<String, Any>
    ): ResponseEntity<Any> {
        val payloadStr = objectMapper.writeValueAsString(payload)
        if (!verifySignature(sigHeader, payloadStr)) {
            return ResponseEntity.badRequest().body(mapOf("error" to "invalid signature"))
        }
        val eventId = payload["id"] as? String ?: return ResponseEntity.badRequest().body(mapOf("error" to "missing event id"))
        val type = payload["type"] as? String ?: "unknown"
        if (stripeEventRepo.existsById(eventId)) {
            return ResponseEntity.ok(mapOf("idempotent" to true))
        }
        stripeEventRepo.save(StripeEvent(eventId, type))
        // TODO: handle payment_intent.succeeded, update booking/payment status
        return ResponseEntity.ok(mapOf("received" to true))
    }

    fun verifySignature(sigHeader: String?, payload: String): Boolean {
        if (webhookSecret.isNullOrBlank() || sigHeader.isNullOrBlank()) return false
        val parts = sigHeader.split(",")
        val tPart = parts.find { it.trim().startsWith("t=") }?.substringAfter("t=") ?: return false
        val v1 = parts.find { it.trim().startsWith("v1=") }?.substringAfter("v1=") ?: return false
        val signedPayload = "$tPart.$payload"
        val hmac = Mac.getInstance("HmacSHA256")
        val key = SecretKeySpec(webhookSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
        hmac.init(key)
        val sigBytes = hmac.doFinal(signedPayload.toByteArray(StandardCharsets.UTF_8))
        val computed = sigBytes.joinToString("") { "%02x".format(it) }
        return computed.equals(v1, ignoreCase = true)
    }
}

