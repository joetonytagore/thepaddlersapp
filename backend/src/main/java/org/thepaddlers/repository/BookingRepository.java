package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.Booking;

import java.time.OffsetDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Use end-exclusive overlap check so back-to-back bookings are allowed
    // Overlap if existing.start < requested.end AND existing.end > requested.start
    List<Booking> findByCourtIdAndStartAtLessThanAndEndAtGreaterThan(Long courtId, OffsetDateTime end, OffsetDateTime start);

    // Keep inclusive overlap signature too (some code may still reference it)
    List<Booking> findByCourtIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(Long courtId, OffsetDateTime end, OffsetDateTime start);
}
