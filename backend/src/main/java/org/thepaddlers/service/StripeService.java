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

    /**
     * Create a Checkout Session via Stripe REST API. Returns parsed JSON Map.
     */
    public Map<String, Object> createCheckoutSession(Long amount, String currency, String successUrl, String cancelUrl) throws Exception {
        if (stripeSecret == null || stripeSecret.isBlank()) {
            throw new IllegalStateException("Stripe secret is not configured");
        }
        // Build form values; price_data for a single-line item
        StringBuilder form = new StringBuilder();
        form.append("payment_method_types[]=card");
        form.append("&mode=payment");
        form.append("&line_items[0][price_data][currency]=").append(URLEncoder.encode(currency, StandardCharsets.UTF_8));
        form.append("&line_items[0][price_data][product_data][name]=").append(URLEncoder.encode("Booking", StandardCharsets.UTF_8));
        form.append("&line_items[0][price_data][unit_amount]=").append(URLEncoder.encode(String.valueOf(amount), StandardCharsets.UTF_8));
        form.append("&line_items[0][quantity]=1");
        form.append("&success_url=").append(URLEncoder.encode(successUrl, StandardCharsets.UTF_8));
        form.append("&cancel_url=").append(URLEncoder.encode(cancelUrl, StandardCharsets.UTF_8));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.stripe.com/v1/checkout/sessions"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + stripeSecret)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form.toString()))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            Map<String, Object> parsed = mapper.readValue(resp.body(), Map.class);
            return parsed;
        } else {
            throw new RuntimeException("Stripe API returned status=" + resp.statusCode() + " body=" + resp.body());
        }
    }

    /**
     * Create a Customer in Stripe and return parsed JSON response.
     */
    public Map<String, Object> createCustomer(String email) throws Exception {
        if (stripeSecret == null || stripeSecret.isBlank()) {
            throw new IllegalStateException("Stripe secret is not configured");
        }
        String form = "email=" + URLEncoder.encode(email == null ? "" : email, StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.stripe.com/v1/customers"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + stripeSecret)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return mapper.readValue(resp.body(), Map.class);
        } else {
            throw new RuntimeException("Stripe API returned status=" + resp.statusCode() + " body=" + resp.body());
        }
    }

    /**
     * Create a subscription for a customer using a price id. Returns parsed Stripe response.
     */
    public Map<String, Object> createSubscription(String customerId, String priceId) throws Exception {
        if (stripeSecret == null || stripeSecret.isBlank()) {
            throw new IllegalStateException("Stripe secret is not configured");
        }
        StringBuilder form = new StringBuilder();
        form.append("customer=").append(URLEncoder.encode(customerId, StandardCharsets.UTF_8));
        form.append("&items[0][price]=").append(URLEncoder.encode(priceId, StandardCharsets.UTF_8));
        form.append("&expand[]=latest_invoice.payment_intent");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.stripe.com/v1/subscriptions"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + stripeSecret)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form.toString()))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return mapper.readValue(resp.body(), Map.class);
        } else {
            throw new RuntimeException("Stripe API returned status=" + resp.statusCode() + " body=" + resp.body());
        }
    }

    /**
     * Create a Refund on a charge or payment_intent. Returns parsed Stripe response.
     */
    public Map<String, Object> createRefund(String chargeOrPaymentIntentId, Long amountCents, String reason) throws Exception {
        if (stripeSecret == null || stripeSecret.isBlank()) {
            throw new IllegalStateException("Stripe secret is not configured");
        }
        StringBuilder form = new StringBuilder();
        if (chargeOrPaymentIntentId != null && !chargeOrPaymentIntentId.isBlank()) {
            // Stripe accepts payment_intent or charge as 'payment_intent' or 'charge'
            // We will pass payment_intent param so Stripe resolves it
            form.append("payment_intent=").append(URLEncoder.encode(chargeOrPaymentIntentId, StandardCharsets.UTF_8));
        }
        if (amountCents != null) {
            form.append("&amount=").append(URLEncoder.encode(String.valueOf(amountCents), StandardCharsets.UTF_8));
        }
        if (reason != null) {
            form.append("&reason=").append(URLEncoder.encode(reason, StandardCharsets.UTF_8));
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.stripe.com/v1/refunds"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + stripeSecret)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form.toString()))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return mapper.readValue(resp.body(), Map.class);
        } else {
            throw new RuntimeException("Stripe API returned status=" + resp.statusCode() + " body=" + resp.body());
        }
    }
}
