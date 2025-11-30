package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.audit.Audit;
import org.thepaddlers.model.Invoice;
import org.thepaddlers.model.PaymentTransaction;
import org.thepaddlers.repository.InvoiceRepository;
import org.thepaddlers.repository.PaymentTransactionRepository;
import org.thepaddlers.service.StripeService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orgs/{orgId}")
public class RefundController {
    private final StripeService stripeService;
    private final InvoiceRepository invoiceRepository;
    private final PaymentTransactionRepository txRepo;

    public RefundController(StripeService stripeService, InvoiceRepository invoiceRepository, PaymentTransactionRepository txRepo) {
        this.stripeService = stripeService;
        this.invoiceRepository = invoiceRepository;
        this.txRepo = txRepo;
    }

    @PostMapping("/invoices/{invoiceId}/refund")
    @Audit(action = "invoice.refund", entity = "invoice")
    public ResponseEntity<?> refund(@PathVariable Long orgId, @PathVariable Long invoiceId, @RequestBody Map<String, Object> body) {
        try {
            Invoice inv = invoiceRepository.findById(invoiceId).orElse(null);
            if (inv == null) return ResponseEntity.status(404).body(Map.of("error", "invoice not found"));

            // Accept stripePaymentIntentId in body or find from txRepo using invoice metadata
            String paymentIntentId = (String) body.getOrDefault("stripePaymentIntentId", null);
            if (paymentIntentId == null) {
                // try to find a tx for this user/invoice
                // naive: find any tx with bookingId==invoiceId or userId==inv.user.id
                // prefer payment intent id present
                var txs = txRepo.findAll();
                for (PaymentTransaction t : txs) {
                    if (t.getBookingId() != null && t.getBookingId().equals(invoiceId)) {
                        if (t.getStripePaymentIntentId() != null) { paymentIntentId = t.getStripePaymentIntentId(); break; }
                        if (t.getStripeObjectId() != null) { paymentIntentId = t.getStripeObjectId(); break; }
                    }
                    if (inv.getUser() != null && t.getUserId() != null && t.getUserId().equals(inv.getUser().getId())) {
                        if (t.getStripePaymentIntentId() != null) { paymentIntentId = t.getStripePaymentIntentId(); break; }
                    }
                }
            }

            if (paymentIntentId == null) return ResponseEntity.status(400).body(Map.of("error", "stripePaymentIntentId required"));

            Long amountCents = null; if (body.get("amount_cents") instanceof Number) amountCents = ((Number) body.get("amount_cents")).longValue();
            String reason = (String) body.getOrDefault("reason", "requested_by_customer");

            Map<String, Object> refundResp = stripeService.createRefund(paymentIntentId, amountCents, reason);

            // store a PaymentTransaction record representing the refund
            PaymentTransaction rtx = new PaymentTransaction();
            rtx.setStripeEventId((String) refundResp.getOrDefault("id", null));
            rtx.setEventType("refund.created");
            rtx.setStripeObjectId((String) refundResp.getOrDefault("id", null));
            rtx.setAmount(amountCents);
            rtx.setCurrency((String) refundResp.getOrDefault("currency", null));
            rtx.setStatus(String.valueOf(refundResp.getOrDefault("status", "succeeded")));
            if (inv.getUser() != null) rtx.setUserId(inv.getUser().getId());
            txRepo.save(rtx);

            // mark invoice as refunded
            inv.setStatus("REFUNDED");
            invoiceRepository.save(inv);

            return ResponseEntity.ok(Map.of("refund", refundResp, "invoice", inv));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

