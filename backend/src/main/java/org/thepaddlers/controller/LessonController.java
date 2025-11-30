package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.model.Lesson;
import org.thepaddlers.service.LessonService;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LessonController {
    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping("/lessons")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Long studentId = body.get("studentId") == null ? null : Long.valueOf(body.get("studentId").toString());
        Long proId = Long.valueOf(body.get("proId").toString());
        OffsetDateTime startsAt = OffsetDateTime.parse(body.get("startsAt").toString());
        OffsetDateTime endsAt = OffsetDateTime.parse(body.get("endsAt").toString());
        Double price = body.get("price") == null ? null : Double.valueOf(body.get("price").toString());

        if (studentId == null) return ResponseEntity.badRequest().body("studentId required");

        Lesson l = lessonService.createLesson(studentId, proId, startsAt, endsAt, price);
        return ResponseEntity.status(201).body(l);
    }

    @GetMapping("/lessons/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return lessonService.getLesson(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(404).body(null));
    }

    @PostMapping("/lessons/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long instructorId = body.get("instructorId") == null ? null : Long.valueOf(body.get("instructorId").toString());
        if (instructorId == null) return ResponseEntity.badRequest().body("instructorId required");
        try {
            Lesson l = lessonService.acceptLesson(instructorId, id);
            return ResponseEntity.ok(l);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id, @RequestParam("requesterId") Long requesterId) {
        try {
            Lesson l = lessonService.cancelLesson(requesterId, id);
            return ResponseEntity.ok(l);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}

