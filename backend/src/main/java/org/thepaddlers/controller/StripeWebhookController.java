package org.thepaddlers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.service.StripeWebhookService;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {
    private final String stripeWebhookSecret;
    private final StripeWebhookService webhookService;
    private final ObjectMapper mapper = new ObjectMapper();

    public StripeWebhookController(@Value("${stripe.webhook.secret:}") String stripeWebhookSecret, StripeWebhookService webhookService) {
        this.stripeWebhookSecret = stripeWebhookSecret;
        this.webhookService = webhookService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<?> handle(@RequestHeader(name = "Stripe-Signature", required = false) String sigHeader, @RequestBody Map<String, Object> payload) {
        try {
            String payloadStr = mapper.writeValueAsString(payload);
            if (stripeWebhookSecret != null && !stripeWebhookSecret.isBlank()) {
                if (sigHeader == null || sigHeader.isBlank()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "missing signature header"));
                boolean ok = webhookService.verifySignature(sigHeader, payloadStr, 300);
                if (!ok) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "invalid signature"));
            }

            webhookService.handleEventMap(payload);
            return ResponseEntity.ok(Map.of("received", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
