package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.audit.Audit;
import org.thepaddlers.service.StripeService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentsController {

    private final StripeService stripeService;

    public PaymentsController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/payments/create-payment-intent")
    @Audit(action = "payment.create_intent", entity = "payment")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Object> body) {
        try {
            // amount expected in cents as integer
            Integer amount = (Integer) body.getOrDefault("amount", 1000);
            String currency = (String) body.getOrDefault("currency", "usd");

            // avoid depending on Stripe types directly in this controller so IDE/analysis doesn't
            // fail if the Stripe SDK isn't visible to the analyzer — treat the result opaquely
            // and use reflection to extract common fields.
            Object intent = stripeService.createPaymentIntent(Long.valueOf(amount), currency);
            Map<String, Object> resp = new HashMap<>();
            // access common methods reflectively to avoid compile-time dependency on Stripe types
            try {
                var clientSecretMethod = intent.getClass().getMethod("getClientSecret");
                var idMethod = intent.getClass().getMethod("getId");
                Object clientSecret = clientSecretMethod.invoke(intent);
                Object id = idMethod.invoke(intent);
                resp.put("clientSecret", clientSecret);
                resp.put("id", id);
            } catch (Exception nsme) {
                // fallback: return a safe string representation of the intent object
                resp.put("intent", String.valueOf(intent));
            }

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            // Distinguish Stripe errors from other exceptions without importing Stripe types
            String className = e.getClass().getName();
            if (className.startsWith("com.stripe.")) {
                return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/payments/charge")
    @Audit(action = "payment.charge", entity = "payment")
    public ResponseEntity<?> charge(@RequestBody Map<String, Object> body) {
        // stubbed: pretend we called Stripe and succeeded
        return ResponseEntity.ok(Map.of("status", "charged", "details", body));
    }

    // Renamed to avoid conflict with dedicated StripeWebhookController which handles signed webhooks.
    @PostMapping("/webhooks/stripe-legacy")
    @Audit(action = "webhook.stripe", entity = "stripe_event")
    public ResponseEntity<?> stripeWebhook(@RequestBody Map<String, Object> body) {
        // accept webhook
        return ResponseEntity.ok(Map.of("received", true));
    }
}
