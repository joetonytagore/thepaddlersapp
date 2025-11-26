package org.thepaddlers.webhook;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Minimal e2e test that posts a simulated Stripe event to the running backend webhook endpoint.
 * This test is disabled by default because it requires the backend to be running and migrations applied.
 */
@Disabled("Run manually with backend running and Flyway migrations applied")
public class PaymentWebhookE2ETest {

    @Test
    void postSimulatedStripeEvent() throws Exception {
        String json = "{\n" +
                "  \"id\": \"evt_test_e2e_1\",\n" +
                "  \"type\": \"payment_intent.succeeded\",\n" +
                "  \"data\": {\n" +
                "    \"object\": {\n" +
                "      \"id\": \"pi_test_123\",\n" +
                "      \"object\": \"payment_intent\",\n" +
                "      \"metadata\": { \"bookingId\": \"42\" },\n" +
                "      \"amount\": 1000,\n" +
                "      \"currency\": \"usd\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/api/payments/webhook"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() >= 200 && resp.statusCode() < 300, "Expected 2xx response, got " + resp.statusCode());
    }
}

