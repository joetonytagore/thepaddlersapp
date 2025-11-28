package org.thepaddlers.webhook;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.thepaddlers.model.Booking;
import org.thepaddlers.model.PaymentTransaction;
import org.thepaddlers.repository.BookingRepository;
import org.thepaddlers.repository.PaymentTransactionRepository;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StripeWebhookIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    org.thepaddlers.repository.CourtRepository courtRepository;

    @Autowired
    PaymentTransactionRepository txRepo;

    @BeforeAll
    static void beforeAll() {
        // container is started automatically by Testcontainers
    }

    @AfterAll
    static void afterAll() {
        // cleanup is automatic
    }

    @Test
    void webhook_should_store_transaction_and_update_booking() throws Exception {
        // Insert a court and booking to be updated
        org.thepaddlers.model.Court court = new org.thepaddlers.model.Court();
        court.setName("Test Court");
        court = courtRepository.save(court);

        Booking b = new Booking();
        b.setId(100L);
        b.setCourt(court);
        b.setStatus("CONFIRMED");
        bookingRepository.save(b);

        Map<String, Object> event = Map.of(
                "id", "evt_integ_1",
                "type", "payment_intent.succeeded",
                "data", Map.of("object", Map.of("id", "pi_integ_1", "object", "payment_intent", "metadata", Map.of("bookingId", "100"), "amount", 1500, "currency", "usd"))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String,Object>> request = new HttpEntity<>(event, headers);

        ResponseEntity<Map> resp = restTemplate.postForEntity("/api/webhooks/stripe", request, Map.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());

        // booking should be updated to PAID
        Optional<Booking> saved = bookingRepository.findById(100L);
        assertTrue(saved.isPresent());
        assertEquals("PAID", saved.get().getStatus());

        // transaction should exist
        Optional<PaymentTransaction> tx = txRepo.findByStripeEventId("evt_integ_1");
        assertTrue(tx.isPresent());
        assertEquals("pi_integ_1", tx.get().getStripeObjectId());
        assertEquals(1500L, tx.get().getAmount());
    }
}
