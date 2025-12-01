package org.thepaddlers;

import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.Test;
import org.thepaddlers.controller.PaymentsController;
import org.thepaddlers.service.StripeService;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PaymentsControllerTest {

    @Test
    public void testCreatePaymentIntent() throws Exception {
        StripeService mockStripe = mock(StripeService.class);
        // Mock the client secret response
        when(mockStripe.createPaymentIntent(any())).thenReturn("test_client_secret");
        PaymentsController ctrl = new PaymentsController(mockStripe);
        Map<String, Object> body = Map.of("amount", 1000, "currency","usd");
        ResponseEntity<?> res = ctrl.createIntent(body);
        assertEquals(200, res.getStatusCodeValue());
        Map<String, Object> payload = (Map<String, Object>) res.getBody();
        assertEquals("test_client_secret", payload.get("client_secret"));
    }
}
