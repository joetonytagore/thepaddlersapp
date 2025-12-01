package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.service.StripeService;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {
    private final StripeService stripeService;

    public PaymentsController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<?> createIntent(@RequestBody Map<String, Object> body) {
        // Expect amount, currency, userId, etc. in body
        try {
            String clientSecret = stripeService.createPaymentIntent(body);
            return ResponseEntity.ok(Map.of("client_secret", clientSecret));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
