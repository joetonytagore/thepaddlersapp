package org.thepaddlers.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User pro;

    @ManyToOne
    private User student;

    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private Double price;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getPro() { return pro; }
    public void setPro(User pro) { this.pro = pro; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public OffsetDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(OffsetDateTime startsAt) { this.startsAt = startsAt; }
    public OffsetDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(OffsetDateTime endsAt) { this.endsAt = endsAt; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

