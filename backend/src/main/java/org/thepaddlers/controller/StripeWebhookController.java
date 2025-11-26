package org.thepaddlers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.service.MembershipService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@ConditionalOnExpression("'${stripe.webhook.secret:}' != ''")
public class StripeWebhookController {
    private final String stripeWebhookSecret;
    private final MembershipService membershipService;
    private final ObjectMapper mapper = new ObjectMapper();

    public StripeWebhookController(@Value("${stripe.webhook.secret:}") String stripeWebhookSecret, MembershipService membershipService) {
        this.stripeWebhookSecret = stripeWebhookSecret;
        this.membershipService = membershipService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<?> handle(@RequestHeader(name = "Stripe-Signature", required = false) String sigHeader, @RequestBody String payload) {
        try {
            // verify signature if secret is configured
            if (stripeWebhookSecret != null && !stripeWebhookSecret.isBlank()) {
                if (sigHeader == null || sigHeader.isBlank()) {
                    return ResponseEntity.status(400).body(Map.of("error", "missing signature header"));
                }
                // Stripe signs: t=timestamp,v1=signature
                long toleranceSec = 300; // 5 minutes
                boolean verified = verifyStripeSignature(sigHeader, payload, stripeWebhookSecret, toleranceSec);
                if (!verified) return ResponseEntity.status(400).body(Map.of("error", "invalid signature"));
            }

            Map<String, Object> data = mapper.readValue(payload, Map.class);
            String eventId = (String) data.get("id");
            String type = (String) data.get("type");
            if (eventId == null) return ResponseEntity.badRequest().body(Map.of("error", "missing id"));

            boolean ok = membershipService.markStripeEventProcessed(eventId, payload);
            if (!ok) return ResponseEntity.ok(Map.of("received", "duplicate"));

            // TODO: dispatch by event type; for now just ack
            return ResponseEntity.ok(Map.of("received", true, "type", type));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    private boolean verifyStripeSignature(String sigHeader, String payload, String secret, long toleranceSeconds) {
        try {
            String[] parts = sigHeader.split(",");
            String tPart = null;
            String v1 = null;
            for (String p : parts) {
                p = p.trim();
                if (p.startsWith("t=")) tPart = p.substring(2);
                if (p.startsWith("v1=")) v1 = p.substring(3);
            }
            if (tPart == null || v1 == null) return false;
            long timestamp = Long.parseLong(tPart);
            long now = Instant.now().getEpochSecond();
            if (Math.abs(now - timestamp) > toleranceSeconds) return false;

            String signedPayload = timestamp + "." + payload;
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(key);
            byte[] sigBytes = hmac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            String computed = bytesToHex(sigBytes);
            // Stripe sends hex lowercase; compare
            return computed.equalsIgnoreCase(v1);
        } catch (Exception e) {
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
