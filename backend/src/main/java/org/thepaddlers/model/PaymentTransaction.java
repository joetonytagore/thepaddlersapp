package org.thepaddlers.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions", uniqueConstraints = {@UniqueConstraint(columnNames = {"stripe_event_id"})})
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UUID for client-level idempotency
    @Column(name = "payment_uuid", columnDefinition = "uuid")
    private UUID paymentUuid;

    @Column(name = "stripe_event_id", unique = true)
    private String stripeEventId;

    // e.g. payment_intent id
    @Column(name = "stripe_object_id")
    private String stripeObjectId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "user_id")
    private Long userId;

    // Stored in cents, maps to amount_cents column
    @Column(name = "amount_cents")
    private Long amount;

    private String currency;

    @Column(name = "status")
    private String status;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    @Lob
    @Column(name = "metadata")
    private String metadata;

    @Lob
    @Column(name = "raw_event_json")
    private String rawEventJson;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getPaymentUuid() { return paymentUuid; }
    public void setPaymentUuid(UUID paymentUuid) { this.paymentUuid = paymentUuid; }

    public String getStripeEventId() { return stripeEventId; }
    public void setStripeEventId(String stripeEventId) { this.stripeEventId = stripeEventId; }

    public String getStripeObjectId() { return stripeObjectId; }
    public void setStripeObjectId(String stripeObjectId) { this.stripeObjectId = stripeObjectId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public void setStripePaymentIntentId(String stripePaymentIntentId) { this.stripePaymentIntentId = stripePaymentIntentId; }

    public String getStripeChargeId() { return stripeChargeId; }
    public void setStripeChargeId(String stripeChargeId) { this.stripeChargeId = stripeChargeId; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getRawEventJson() { return rawEventJson; }
    public void setRawEventJson(String rawEventJson) { this.rawEventJson = rawEventJson; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
