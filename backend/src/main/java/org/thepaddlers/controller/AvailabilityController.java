package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.model.Booking;
import org.thepaddlers.repository.BookingRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AvailabilityController {
    private final BookingRepository bookingRepository;

    public AvailabilityController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/orgs/{orgId}/availability")
    public ResponseEntity<?> availability(@PathVariable Long orgId,
                                          @RequestParam(required = false) Long courtId,
                                          @RequestParam String from,
                                          @RequestParam String to) {
        OffsetDateTime start = OffsetDateTime.parse(from);
        OffsetDateTime end = OffsetDateTime.parse(to);
        // simple: return bookings overlapping the interval (if courtId provided, filter)
        List<Booking> overlapping;
        if (courtId != null) {
            overlapping = bookingRepository.findByCourtIdAndStartAtLessThanAndEndAtGreaterThan(courtId, end, start);
        } else {
            overlapping = bookingRepository.findAll().stream().filter(b ->
                    !(b.getEndAt().isBefore(start) || b.getStartAt().isAfter(end))
            ).toList();
        }
        return ResponseEntity.ok(Map.of("bookings", overlapping));
    }

    @PostMapping("/orgs/{orgId}/reservations")
    public ResponseEntity<?> createReservation(@PathVariable Long orgId, @RequestBody Booking input) {
        // basic validation and reuse BookingController logic could be better
        if (input.getCourt() == null || input.getUser() == null || input.getStartAt() == null || input.getEndAt() == null) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }
        // naive: save
        Booking saved = bookingRepository.save(input);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/reservations/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        bookingRepository.findById(id).ifPresent(b -> { b.setStatus("CANCELLED"); bookingRepository.save(b); });
        return ResponseEntity.ok(Map.of("ok", true));
    }
}

