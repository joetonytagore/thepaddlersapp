package org.thepaddlers.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
public class WaitlistEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Court court;

    @ManyToOne
    private User user;

    private OffsetDateTime requestedAt;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Court getCourt() { return court; }
    public void setCourt(Court court) { this.court = court; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public OffsetDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(OffsetDateTime requestedAt) { this.requestedAt = requestedAt; }
}

