package org.thepaddlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.thepaddlers.model.Booking;
import org.thepaddlers.model.Court;
import org.thepaddlers.model.User;
import org.thepaddlers.repository.BookingRepository;
import org.thepaddlers.repository.CourtRepository;
import org.thepaddlers.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StripeWebhookControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CourtRepository courtRepository;
    @Autowired
    private UserRepository userRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testInvoicePaymentSucceededWebhook() throws Exception {
        Court court = new Court();
        court.setName("StripeCourt");
        court = courtRepository.save(court);
        User user = new User();
        user.setName("StripeUser");
        user.setEmail("stripeuser@example.com");
        user = userRepository.save(user);
        Booking booking = new Booking();
        booking.setCourt(court);
        booking.setUser(user);
        booking.setStartAt(OffsetDateTime.now().plusDays(1));
        booking.setEndAt(booking.getStartAt().plusHours(1));
        booking = bookingRepository.save(booking);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("bookingId", booking.getId());
        Map<String, Object> paymentIntent = new HashMap<>();
        paymentIntent.put("object", "payment_intent");
        paymentIntent.put("id", "pi_test_123");
        paymentIntent.put("amount", 1000);
        paymentIntent.put("currency", "usd");
        paymentIntent.put("metadata", metadata);
        Map<String, Object> data = new HashMap<>();
        data.put("object", paymentIntent);
        Map<String, Object> event = new HashMap<>();
        event.put("id", "evt_test_123");
        event.put("type", "payment_intent.succeeded");
        event.put("data", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Signature verification is skipped if secret is blank
        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(event), headers);
        ResponseEntity<Map> response = restTemplate.postForEntity("/api/webhooks/stripe", entity, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Booking updated = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals("PAID", updated.getStatus());
    }
}

