package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.audit.Audit;
import org.thepaddlers.model.Booking;
import org.thepaddlers.model.Court;
import org.thepaddlers.model.User;
import org.thepaddlers.model.WaitlistEntry;
import org.thepaddlers.repository.BookingRepository;
import org.thepaddlers.repository.CourtRepository;
import org.thepaddlers.repository.UserRepository;
import org.thepaddlers.repository.WaitlistRepository;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;
    private final WaitlistRepository waitlistRepository;

    public BookingController(BookingRepository bookingRepository, CourtRepository courtRepository, UserRepository userRepository, WaitlistRepository waitlistRepository) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.userRepository = userRepository;
        this.waitlistRepository = waitlistRepository;
    }

    @PostMapping
    @Audit(action = "booking.create", entity = "booking")
    public ResponseEntity<?> create(@RequestBody Booking input, @RequestHeader(value = "X-WAITLIST", required = false) String waitlistHeader) {
        // basic validation
        if (input.getCourt() == null || input.getUser() == null || input.getStartAt() == null || input.getEndAt() == null) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }

        Optional<Court> courtOpt = courtRepository.findById(input.getCourt().getId());
        Optional<User> userOpt = userRepository.findById(input.getUser().getId());
        if (courtOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid court or user");
        }

        // conflict check (end-exclusive semantics)
        List<Booking> overlaps = bookingRepository.findByCourtIdAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                courtOpt.get().getId(), input.getEndAt(), input.getStartAt());
        if (!overlaps.isEmpty()) {
            // If caller requested waitlist (X-WAITLIST: true), create a waitlist entry instead of returning 409
            if ("true".equalsIgnoreCase(waitlistHeader)) {
                WaitlistEntry e = new WaitlistEntry();
                e.setCourt(courtOpt.get());
                e.setUser(userOpt.get());
                e.setRequestedAt(OffsetDateTime.now());
                WaitlistEntry saved = waitlistRepository.save(e);
                return ResponseEntity.accepted().body(saved);
            }
            return ResponseEntity.status(409).body("Time slot conflict");
        }

        Booking booking = new Booking();
        booking.setCourt(courtOpt.get());
        booking.setUser(userOpt.get());
        booking.setStartAt(input.getStartAt());
        booking.setEndAt(input.getEndAt());

        Booking saved = bookingRepository.save(booking);
        return ResponseEntity.created(URI.create("/api/bookings/" + saved.getId())).body(saved);
    }

    // New: list bookings (optional filters: userId, courtId)
    @GetMapping
    public ResponseEntity<List<Booking>> list(@RequestParam(value = "userId", required = false) Long userId,
                                              @RequestParam(value = "courtId", required = false) Long courtId) {
        if (userId != null) {
            List<Booking> byUser = bookingRepository.findAll().stream().filter(b -> b.getUser() != null && userId.equals(b.getUser().getId())).toList();
            return ResponseEntity.ok(byUser);
        }
        if (courtId != null) {
            List<Booking> byCourt = bookingRepository.findAll().stream().filter(b -> b.getCourt() != null && courtId.equals(b.getCourt().getId())).toList();
            return ResponseEntity.ok(byCourt);
        }
        return ResponseEntity.ok(bookingRepository.findAll());
    }
}
