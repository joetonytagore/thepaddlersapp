package org.thepaddlers.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
public class EventRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private Status status = Status.REGISTERED;

    private OffsetDateTime createdAt;
    private Integer waitlistPosition;
    private OffsetDateTime waitlistOfferExpiresAt;

    public enum Status {
        REGISTERED, WAITLISTED, OFFERED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getWaitlistPosition() { return waitlistPosition; }
    public void setWaitlistPosition(Integer waitlistPosition) { this.waitlistPosition = waitlistPosition; }
    public OffsetDateTime getWaitlistOfferExpiresAt() { return waitlistOfferExpiresAt; }
    public void setWaitlistOfferExpiresAt(OffsetDateTime waitlistOfferExpiresAt) { this.waitlistOfferExpiresAt = waitlistOfferExpiresAt; }
}

