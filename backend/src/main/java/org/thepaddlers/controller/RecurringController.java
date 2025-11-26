package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class RecurringController {

    @PostMapping("/recurring")
    public ResponseEntity<?> createRecurring(@RequestBody Map<String, Object> body) {
        // stub: accept recurrence request and return a placeholder
        return ResponseEntity.accepted().body(Map.of("status", "scheduled", "details", body));
    }
}

