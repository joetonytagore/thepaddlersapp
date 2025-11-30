package org.thepaddlers.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "booking_rules")
public class BookingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Court court;

    private LocalTime businessHourStart;
    private LocalTime businessHourEnd;

    private LocalTime memberOnlyStart;
    private LocalTime memberOnlyEnd;

    private Integer maxDurationMinutes;
    private Integer minLeadTimeMinutes;
    private Integer maxLeadTimeDays;

    private String cancellationPolicy; // e.g. "24h full refund, <24h 50% fee"

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Court getCourt() { return court; }
    public void setCourt(Court court) { this.court = court; }
    public LocalTime getBusinessHourStart() { return businessHourStart; }
    public void setBusinessHourStart(LocalTime businessHourStart) { this.businessHourStart = businessHourStart; }
    public LocalTime getBusinessHourEnd() { return businessHourEnd; }
    public void setBusinessHourEnd(LocalTime businessHourEnd) { this.businessHourEnd = businessHourEnd; }
    public LocalTime getMemberOnlyStart() { return memberOnlyStart; }
    public void setMemberOnlyStart(LocalTime memberOnlyStart) { this.memberOnlyStart = memberOnlyStart; }
    public LocalTime getMemberOnlyEnd() { return memberOnlyEnd; }
    public void setMemberOnlyEnd(LocalTime memberOnlyEnd) { this.memberOnlyEnd = memberOnlyEnd; }
    public Integer getMaxDurationMinutes() { return maxDurationMinutes; }
    public void setMaxDurationMinutes(Integer maxDurationMinutes) { this.maxDurationMinutes = maxDurationMinutes; }
    public Integer getMinLeadTimeMinutes() { return minLeadTimeMinutes; }
    public void setMinLeadTimeMinutes(Integer minLeadTimeMinutes) { this.minLeadTimeMinutes = minLeadTimeMinutes; }
    public Integer getMaxLeadTimeDays() { return maxLeadTimeDays; }
    public void setMaxLeadTimeDays(Integer maxLeadTimeDays) { this.maxLeadTimeDays = maxLeadTimeDays; }
    public String getCancellationPolicy() { return cancellationPolicy; }
    public void setCancellationPolicy(String cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }
}
