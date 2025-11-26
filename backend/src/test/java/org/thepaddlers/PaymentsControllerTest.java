package org.thepaddlers;

import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.Test;
import org.thepaddlers.controller.PaymentsController;
import org.thepaddlers.service.StripeService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentsControllerTest {

    @Test
    public void testCreatePaymentIntent() throws Exception {
        StripeService mockStripe = mock(StripeService.class);
        PaymentIntent fake = new PaymentIntent();
        fake.setId("pi_test_123");
        // use reflection to set client_secret since setter not exposed (use map hack)
        // PaymentIntent has getClientSecret but not a public setter; we can mock via Mockito when calling.
        when(mockStripe.createPaymentIntent(1000L, "usd")).thenReturn(fake);

        PaymentsController ctrl = new PaymentsController(mockStripe);
        Map<String, Object> body = Map.of("amount", 1000, "currency","usd");
        var res = ctrl.createPaymentIntent(body);
        assertEquals(200, res.getStatusCodeValue());
        Map<String, Object> payload = (Map<String, Object>) res.getBody();
        assertNotNull(payload.get("id"));
        assertEquals("pi_test_123", payload.get("id"));
    }
}

