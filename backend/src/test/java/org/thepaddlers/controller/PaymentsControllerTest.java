package org.thepaddlers.controller;

import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.thepaddlers.service.StripeService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(PaymentsController.class)
public class PaymentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StripeService stripeService;

    @Test
    public void testCreatePaymentIntent() throws Exception {
        PaymentIntent fake = org.mockito.Mockito.mock(PaymentIntent.class);
        when(fake.getClientSecret()).thenReturn("cs_test_123");
        when(fake.getId()).thenReturn("pi_test_123");

        when(stripeService.createPaymentIntent(1000L, "usd")).thenReturn(fake);

        String body = "{\"amount\":1000,\"currency\":\"usd\"}";

        mockMvc.perform(post("/api/payments/create-payment-intent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientSecret", is("cs_test_123")))
                .andExpect(jsonPath("$.id", is("pi_test_123")));
    }
}

