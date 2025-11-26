package org.thepaddlers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Service
public class StripeService {
    private final String stripeSecret;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public StripeService(@Value("${stripe.secret:}") String stripeSecret) {
        this.stripeSecret = stripeSecret;
    }

    /**
     * Create a PaymentIntent via Stripe REST API. Returns the parsed JSON response as a Map.
     * This implementation avoids a compile-time dependency on the Stripe Java SDK.
     */
    public Object createPaymentIntent(Long amount, String currency) throws Exception {
        if (stripeSecret == null || stripeSecret.isBlank()) {
            throw new IllegalStateException("Stripe secret is not configured");
        }

        String form = "amount=" + URLEncoder.encode(String.valueOf(amount), StandardCharsets.UTF_8)
                + "&currency=" + URLEncoder.encode(currency, StandardCharsets.UTF_8)
                + "&automatic_payment_methods[enabled]=true";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.stripe.com/v1/payment_intents"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + stripeSecret)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            Map<String, Object> parsed = mapper.readValue(resp.body(), Map.class);
            return parsed;
        } else {
            throw new RuntimeException("Stripe API returned status=" + resp.statusCode() + " body=" + resp.body());
        }
    }
}
