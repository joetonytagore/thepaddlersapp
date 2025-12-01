package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.repository.BookingRepository;
import org.thepaddlers.repository.UserRepository;
import org.thepaddlers.repository.MessageRepository;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
public class MobileController {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public MobileController(BookingRepository bookingRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping("/home")
    public ResponseEntity<?> getHome(@RequestParam Long userId) {
        // Aggregate upcoming bookings, messages, quick actions
        List<?> bookings = bookingRepository.findUpcomingForUser(userId, 5); // compact list
        List<?> messages = messageRepository.findRecentForUser(userId, 5); // compact list
        // Quick actions can be static or dynamic
        List<Map<String, Object>> quickActions = List.of(
            Map.of("action", "book_court", "label", "Book Court"),
            Map.of("action", "view_waitlist", "label", "Waitlist"),
            Map.of("action", "contact_instructor", "label", "Contact Instructor")
        );
        return ResponseEntity.ok(Map.of(
            "bookings", bookings,
            "messages", messages,
            "quickActions", quickActions
        ));
    }
}

