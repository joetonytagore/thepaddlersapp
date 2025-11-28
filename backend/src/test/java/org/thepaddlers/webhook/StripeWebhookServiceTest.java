package org.thepaddlers.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.thepaddlers.model.Booking;
import org.thepaddlers.model.PaymentTransaction;
import org.thepaddlers.repository.BookingRepository;
import org.thepaddlers.repository.PaymentTransactionRepository;
import org.thepaddlers.service.AuditService;
import org.thepaddlers.service.StripeWebhookService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class StripeWebhookServiceTest {
    BookingRepository bookingRepo;
    PaymentTransactionRepository txRepo;
    AuditService auditService;

    StripeWebhookService svc;

    @BeforeEach
    void setup() {
        bookingRepo = Mockito.mock(BookingRepository.class);
        txRepo = Mockito.mock(PaymentTransactionRepository.class);
        auditService = Mockito.mock(AuditService.class);
        svc = new StripeWebhookService("whsec_test", txRepo, bookingRepo, auditService);
    }

    @Test
    void verifySignature_happyPath() {
        // prepare a payload and signature generated using the same secret
        String payload = "{\"id\":\"evt_1\",\"type\":\"payment_intent.succeeded\"}";
        long t = System.currentTimeMillis() / 1000L;
        String signed = t + "." + payload;
        try {
            javax.crypto.Mac hmac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec key = new javax.crypto.spec.SecretKeySpec("whsec_test".getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(key);
            byte[] sig = hmac.doFinal(signed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(); for (byte b : sig) sb.append(String.format("%02x", b));
            String v1 = sb.toString();
            String header = "t=" + t + ",v1=" + v1;
            assertTrue(svc.verifySignature(header, payload, 300));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void handleEventMap_updatesBookingAndPersistsTx() throws Exception {
        Booking b = new Booking();
        b.setId(42L);
        b.setStatus("CONFIRMED");
        when(bookingRepo.findById(42L)).thenReturn(Optional.of(b));
        when(txRepo.findByStripeEventId(any())).thenReturn(Optional.empty());
        when(txRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String,Object> event = Map.of(
                "id", "evt_test_1",
                "type", "payment_intent.succeeded",
                "data", Map.of("object", Map.of("id", "pi_123", "object", "payment_intent", "metadata", Map.of("bookingId", "42"), "amount", 1000, "currency", "usd"))
        );

        svc.handleEventMap(event);

        // verify booking status saved
        verify(bookingRepo, times(1)).save(any());
        // verify transaction saved at least once
        verify(txRepo, atLeastOnce()).save(any());
        // verify audit recorded
        verify(auditService, times(1)).record(any());
        assertEquals("PAID", b.getStatus());
    }
}

