package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.Booking;

import java.time.OffsetDateTime;
import java.util.List;

// Deprecated: Remove this file to resolve duplicate BookingRepository build error.
// Please use the Kotlin version in src/main/kotlin/org/thepaddlers/repository/BookingRepository.kt
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Use end-exclusive overlap check so back-to-back bookings are allowed
    // Overlap if existing.start < requested.end AND existing.end > requested.start
    List<Booking> findByCourtIdAndStartAtLessThanAndEndAtGreaterThan(Long courtId, OffsetDateTime end, OffsetDateTime start);

    // Keep inclusive overlap signature too (some code may still reference it)
    List<Booking> findByCourtIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(Long courtId, OffsetDateTime end, OffsetDateTime start);

    List<Booking> findUpcomingForUser(Long userId, int limit);
}
