package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
import org.thepaddlers.api.dto.ErrorResponse;
import org.thepaddlers.model.Membership;
import org.thepaddlers.repository.MembershipRepository;
import org.thepaddlers.model.BookingRule;
import org.thepaddlers.repository.BookingRuleRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;
    private final WaitlistRepository waitlistRepository;
    private final EntityManager entityManager;
    private final MembershipRepository membershipRepository;
    private final BookingRuleRepository bookingRuleRepository;

    public BookingController(BookingRepository bookingRepository, CourtRepository courtRepository, UserRepository userRepository, WaitlistRepository waitlistRepository, EntityManager entityManager, MembershipRepository membershipRepository, BookingRuleRepository bookingRuleRepository) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.userRepository = userRepository;
        this.waitlistRepository = waitlistRepository;
        this.entityManager = entityManager;
        this.membershipRepository = membershipRepository;
        this.bookingRuleRepository = bookingRuleRepository;
    }

    @Timed(value = "booking.create", description = "Time taken to create a booking")
    @PostMapping
    @Audit(action = "booking.create", entity = "booking")
    @Transactional
    public ResponseEntity<?> create(@RequestBody Booking input, @RequestHeader(value = "X-WAITLIST", required = false) String waitlistHeader) {
        // basic validation
        if (input.getCourt() == null || input.getUser() == null || input.getStartAt() == null || input.getEndAt() == null) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }

        // Load and lock the court row so concurrent creates for the same court serialize here.
        Court court = entityManager.find(Court.class, input.getCourt().getId(), LockModeType.PESSIMISTIC_WRITE);
        if (court == null) {
            return ResponseEntity.badRequest().body("Invalid court");
        }
        Optional<User> userOpt = userRepository.findById(input.getUser().getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid user");
        }

        // conflict check (end-exclusive semantics)
        List<Booking> overlaps = bookingRepository.findByCourtIdAndStartAtLessThanAndEndAtGreaterThan(
                court.getId(), input.getEndAt(), input.getStartAt());
        if (!overlaps.isEmpty()) {
            // If caller requested waitlist (X-WAITLIST: true), create a waitlist entry instead of returning 409
            if ("true".equalsIgnoreCase(waitlistHeader)) {
                WaitlistEntry e = new WaitlistEntry();
                e.setCourt(court);
                e.setUser(userOpt.get());
                e.setRequestedAt(OffsetDateTime.now());
                WaitlistEntry saved = waitlistRepository.save(e);
                return ResponseEntity.accepted().body(saved);
            }
            return ResponseEntity.status(409).body("Time slot conflict");
        }

        // membership credits enforcement: if user has an active membership for this org, decrement credits
        List<Membership> memberships = membershipRepository.findByUserId(userOpt.get().getId());
        Membership activeMembership = memberships.stream().filter(m -> "ACTIVE".equalsIgnoreCase(m.getStatus())).findFirst().orElse(null);
        if (activeMembership != null) {
            if (activeMembership.getCreditsRemaining() == null || activeMembership.getCreditsRemaining() <= 0) {
                return ResponseEntity.status(402).body("Membership credits exhausted");
            }
            // deduct one credit
            activeMembership.setCreditsRemaining(activeMembership.getCreditsRemaining() - 1);
            membershipRepository.save(activeMembership);
        }

        // Load booking rules for this court
        List<BookingRule> rules = bookingRuleRepository.findByCourt(court);
        BookingRule rule = rules.isEmpty() ? null : rules.get(0);
        if (rule != null) {
            // Business hours enforcement
            LocalTime startTime = input.getStartAt().toLocalTime();
            LocalTime endTime = input.getEndAt().toLocalTime();
            if (startTime.isBefore(rule.getBusinessHourStart()) || endTime.isAfter(rule.getBusinessHourEnd())) {
                return ResponseEntity.status(403).body("Outside business hours");
            }
            // Member-only window enforcement
            boolean isMember = activeMembership != null;
            if (rule.getMemberOnlyStart() != null && rule.getMemberOnlyEnd() != null) {
                if ((startTime.isAfter(rule.getMemberOnlyStart()) && endTime.isBefore(rule.getMemberOnlyEnd())) && !isMember) {
                    return ResponseEntity.status(403).body("Member-only booking window");
                }
            }
            // Max duration enforcement
            long durationMinutes = java.time.Duration.between(input.getStartAt(), input.getEndAt()).toMinutes();
            if (rule.getMaxDurationMinutes() != null && durationMinutes > rule.getMaxDurationMinutes()) {
                return ResponseEntity.status(403).body("Exceeds max booking duration");
            }
            // Lead time enforcement
            long leadTimeMinutes = java.time.Duration.between(OffsetDateTime.now(), input.getStartAt()).toMinutes();
            if (rule.getMinLeadTimeMinutes() != null && leadTimeMinutes < rule.getMinLeadTimeMinutes()) {
                return ResponseEntity.status(403).body("Booking too soon");
            }
            if (rule.getMaxLeadTimeDays() != null && leadTimeMinutes > rule.getMaxLeadTimeDays() * 24 * 60) {
                return ResponseEntity.status(403).body("Booking too far in advance");
            }
        }

        Booking booking = new Booking();
        booking.setCourt(court);
        booking.setUser(userOpt.get());
        booking.setStartAt(input.getStartAt());
        booking.setEndAt(input.getEndAt());

        Booking saved = bookingRepository.save(booking);
        return ResponseEntity.created(URI.create("/api/bookings/" + saved.getId())).body(saved);
    }

    // New: booking details
    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable("id") Long id) {
        Optional<Booking> b = bookingRepository.findById(id);
        if (b.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "not found"));
        return ResponseEntity.ok(b.get());
    }

    // New: cancel booking (soft cancel)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable("id") Long id) {
        Optional<Booking> b = bookingRepository.findById(id);
        if (b.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "not found"));
        Booking booking = b.get();
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        return ResponseEntity.ok(Map.of("status", "cancelled"));
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

    // Admin endpoint to configure booking rules per court/location
    @PostMapping("/rules")
    @Audit(action = "bookingrule.create", entity = "booking_rule")
    public ResponseEntity<?> createRule(@RequestBody BookingRule rule) {
        BookingRule saved = bookingRuleRepository.save(rule);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/rules/{courtId}")
    public ResponseEntity<?> getRules(@PathVariable Long courtId) {
        Court court = courtRepository.findById(courtId).orElse(null);
        if (court == null) return ResponseEntity.status(404).body("Court not found");
        List<BookingRule> rules = bookingRuleRepository.findByCourt(court);
        return ResponseEntity.ok(rules);
    }
}
