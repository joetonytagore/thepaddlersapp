package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.model.InstructorAvailability;
import org.thepaddlers.service.InstructorAvailabilityService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InstructorAvailabilityController {
    private final InstructorAvailabilityService availabilityService;

    public InstructorAvailabilityController(InstructorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/instructors/{instructorId}/availability")
    public ResponseEntity<?> list(@PathVariable Long instructorId, @RequestParam("from") String from, @RequestParam("to") String to) {
        OffsetDateTime f = OffsetDateTime.parse(from);
        OffsetDateTime t = OffsetDateTime.parse(to);
        List<InstructorAvailability> list = availabilityService.listAvailability(instructorId, f, t);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/instructors/{instructorId}/availability")
    public ResponseEntity<?> create(@PathVariable Long instructorId, @RequestBody Map<String, Object> body) {
        OffsetDateTime startAt = OffsetDateTime.parse(body.get("startAt").toString());
        OffsetDateTime endAt = OffsetDateTime.parse(body.get("endAt").toString());
        String type = body.get("type") == null ? "BLOCKED" : body.get("type").toString();
        String notes = body.get("notes") == null ? null : body.get("notes").toString();
        InstructorAvailability a = availabilityService.createAvailability(instructorId, startAt, endAt, InstructorAvailability.AvailabilityType.valueOf(type), notes);
        return ResponseEntity.status(201).body(a);
    }

    @DeleteMapping("/availability/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}

