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
    @SuppressWarnings("resource") // HttpClient doesn't need explicit close here in tests
    void postSimulatedStripeEvent() throws Exception {
        String json = """
        {
          "id": "evt_test_e2e_1",
          "type": "payment_intent.succeeded",
          "data": {
            "object": {
              "id": "pi_test_123",
              "object": "payment_intent",
              "metadata": { "bookingId": "42" },
              "amount": 1000,
              "currency": "usd"
            }
          }
        }
        """;

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
