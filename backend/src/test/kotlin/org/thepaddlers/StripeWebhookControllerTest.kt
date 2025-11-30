package org.thepaddlers

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["stripe.webhook.secret=testsecret"])
class StripeWebhookControllerTest {
    @Autowired lateinit var restTemplate: TestRestTemplate
    val mapper = ObjectMapper()

    fun makeSig(payload: String, secret: String = "testsecret"): String {
        val t = "1234567890"
        val signed = "$t.$payload"
        val hmac = javax.crypto.Mac.getInstance("HmacSHA256")
        val key = javax.crypto.spec.SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        hmac.init(key)
        val sig = hmac.doFinal(signed.toByteArray()).joinToString("") { "%02x".format(it) }
        return "t=$t,v1=$sig"
    }

    @Test
    fun `idempotency works and status updates`() {
        val event = mapOf("id" to "evt_test_1", "type" to "payment_intent.succeeded")
        val payloadStr = mapper.writeValueAsString(event)
        val sig = makeSig(payloadStr)
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("Stripe-Signature", sig)
        }
        val entity = HttpEntity(payloadStr, headers)
        val resp1 = restTemplate.postForEntity("/api/webhooks/stripe", entity, Map::class.java)
        assertEquals(true, resp1.body?.get("received"))
        val resp2 = restTemplate.postForEntity("/api/webhooks/stripe", entity, Map::class.java)
        assertEquals(true, resp2.body?.get("idempotent"))
    }
}

