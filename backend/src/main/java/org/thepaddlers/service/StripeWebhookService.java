package org.thepaddlers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thepaddlers.model.Booking;
import org.thepaddlers.model.PaymentTransaction;
import org.thepaddlers.model.AuditLog;
import org.thepaddlers.repository.BookingRepository;
import org.thepaddlers.repository.PaymentTransactionRepository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class StripeWebhookService {
    private final String webhookSecret;
    private final PaymentTransactionRepository txRepo;
    private final BookingRepository bookingRepo;
    private final AuditService auditService;
    private final MembershipService membershipService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StripeWebhookService(@Value("${stripe.webhook.secret:}") String webhookSecret,
                                PaymentTransactionRepository txRepo,
                                BookingRepository bookingRepo,
                                AuditService auditService,
                                MembershipService membershipService) {
        this.webhookSecret = webhookSecret;
        this.txRepo = txRepo;
        this.bookingRepo = bookingRepo;
        this.auditService = auditService;
        this.membershipService = membershipService;
    }

    public boolean verifySignature(String sigHeader, String payload, long toleranceSeconds) {
        try {
            if (webhookSecret == null || webhookSecret.isBlank()) return false;
            String[] parts = sigHeader.split(",");
            String tPart = null;
            String v1 = null;
            for (String p : parts) {
                p = p.trim();
                if (p.startsWith("t=")) tPart = p.substring(2);
                if (p.startsWith("v1=")) v1 = p.substring(3);
            }
            if (tPart == null || v1 == null) return false;
            long timestamp = Long.parseLong(tPart);
            long now = Instant.now().getEpochSecond();
            if (Math.abs(now - timestamp) > toleranceSeconds) return false;
            String signedPayload = tPart + "." + payload;
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(key);
            byte[] sigBytes = hmac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(sigBytes.length * 2);
            for (byte b : sigBytes) sb.append(String.format("%02x", b));
            String computed = sb.toString();
            return computed.equalsIgnoreCase(v1);
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void handleEventMap(Map<String, Object> event) throws Exception {
        String eventId = (String) event.get("id");
        String type = (String) event.get("type");
        if (eventId == null || type == null) return;

        Optional<PaymentTransaction> existing = txRepo.findByStripeEventId(eventId);
        if (existing.isPresent()) return;

        PaymentTransaction tx = new PaymentTransaction();
        tx.setStripeEventId(eventId);
        tx.setEventType(type);
        try {
            tx.setRawEventJson(objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            tx.setRawEventJson(String.valueOf(event));
        }

        try {
            txRepo.save(tx);
        } catch (DataIntegrityViolationException dive) {
            return; // concurrent insertion
        }

        // navigate into data.object if present
        Object data = event.get("data");
        if (data instanceof Map) {
            Object obj = ((Map<?, ?>) data).get("object");
            if (obj instanceof Map) {
                Map<?, ?> objMap = (Map<?, ?>) obj;
                if ("payment_intent".equals(objMap.get("object")) || objMap.get("id") != null) {
                    String piId = (String) objMap.get("id");
                    Object metadataObj = objMap.get("metadata");
                    String bookingIdStr = null;
                    if (metadataObj instanceof Map) {
                        Object b = ((Map<?, ?>) metadataObj).get("bookingId");
                        if (b != null) bookingIdStr = String.valueOf(b);
                    }
                    Long bookingId = null;
                    if (bookingIdStr != null) {
                        try { bookingId = Long.valueOf(bookingIdStr); } catch (Exception ignored) {}
                    }
                    Object amountObj = objMap.get("amount");
                    Long amount = null;
                    if (amountObj instanceof Number) amount = ((Number) amountObj).longValue();
                    String currency = objMap.get("currency") != null ? String.valueOf(objMap.get("currency")) : null;

                    tx.setStripeObjectId(piId);
                    tx.setAmount(amount);
                    tx.setCurrency(currency);
                    tx.setBookingId(bookingId);
                    txRepo.save(tx);

                    if ("payment_intent.succeeded".equals(type)) {
                        if (bookingId != null) {
                            Optional<Booking> bOpt = bookingRepo.findById(bookingId);
                            if (bOpt.isPresent()) {
                                Booking b = bOpt.get();
                                b.setStatus("PAID");
                                bookingRepo.save(b);

                                AuditLog audit = new AuditLog();
                                audit.setActionType("payment.received");
                                audit.setEntityType("booking");
                                audit.setEntityId(String.valueOf(bookingId));
                                audit.setDetails("stripe_event=" + eventId + " payment_intent=" + piId);
                                auditService.record(audit);
                            } else {
                                AuditLog audit = new AuditLog();
                                audit.setActionType("payment.received");
                                audit.setEntityType("booking");
                                audit.setEntityId(String.valueOf(bookingId));
                                audit.setDetails("booking not found for payment_intent=" + piId);
                                auditService.record(audit);
                            }
                        } else {
                            AuditLog audit = new AuditLog();
                            audit.setActionType("payment.received");
                            audit.setEntityType("stripe_event");
                            audit.setEntityId(eventId);
                            audit.setDetails("payment_intent without booking metadata: " + piId);
                            auditService.record(audit);
                        }
                    } else if ("payment_intent.payment_failed".equals(type)) {
                        if (bookingId != null) {
                            Optional<Booking> bOpt = bookingRepo.findById(bookingId);
                            if (bOpt.isPresent()) {
                                Booking b = bOpt.get();
                                b.setStatus("PAYMENT_FAILED");
                                bookingRepo.save(b);

                                AuditLog audit = new AuditLog();
                                audit.setActionType("payment.failed");
                                audit.setEntityType("booking");
                                audit.setEntityId(String.valueOf(bookingId));
                                audit.setDetails("stripe_event=" + eventId + " payment_intent=" + piId);
                                auditService.record(audit);
                            }
                        }
                    } else {
                        AuditLog audit = new AuditLog();
                        audit.setActionType("webhook.ignored");
                        audit.setEntityType("stripe_event");
                        audit.setEntityId(eventId);
                        audit.setDetails("ignored event type=" + type);
                        auditService.record(audit);
                    }
                } else if ("invoice".equals(objMap.get("object"))) {
                    // handle subscription/invoice events
                    String invoiceId = (String) objMap.get("id");
                    Object subObj = objMap.get("subscription");
                    String subscriptionId = subObj != null ? String.valueOf(subObj) : null;
                    Object paidObj = objMap.get("paid");
                    boolean paid = paidObj instanceof Boolean ? (Boolean) paidObj : false;
                    // period info
                    long periodStart = 0;
                    long periodEnd = 0;
                    Object lines = objMap.get("lines");
                    Object periodObj = objMap.get("period");
                    Object periodStartObj = objMap.get("period_start");
                    Object periodEndObj = objMap.get("period_end");
                    if (periodStartObj instanceof Number) periodStart = ((Number) periodStartObj).longValue();
                    if (periodEndObj instanceof Number) periodEnd = ((Number) periodEndObj).longValue();

                    if ("invoice.payment_succeeded".equals(type) || ("invoice".equals(objMap.get("object")) && paid)) {
                        if (subscriptionId != null) membershipService.handleInvoicePaid(subscriptionId, periodStart, periodEnd);
                        AuditLog audit = new AuditLog();
                        audit.setActionType("invoice.paid");
                        audit.setEntityType("stripe_event");
                        audit.setEntityId(eventId);
                        audit.setDetails("invoice=" + invoiceId + " sub=" + subscriptionId);
                        auditService.record(audit);
                    } else if ("invoice.payment_failed".equals(type)) {
                        if (subscriptionId != null) membershipService.handleInvoiceFailed(subscriptionId);
                        AuditLog audit = new AuditLog();
                        audit.setActionType("invoice.failed");
                        audit.setEntityType("stripe_event");
                        audit.setEntityId(eventId);
                        audit.setDetails("invoice_failed=" + invoiceId + " sub=" + subscriptionId);
                        auditService.record(audit);
                    }
                }
            }
        }
    }
}
