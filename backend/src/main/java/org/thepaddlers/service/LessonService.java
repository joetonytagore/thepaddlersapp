package org.thepaddlers.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thepaddlers.model.Lesson;
import org.thepaddlers.model.User;
import org.thepaddlers.repository.InstructorAvailabilityRepository;
import org.thepaddlers.repository.LessonRepository;
import org.thepaddlers.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LessonService {
    private final LessonRepository lessonRepository;
    private final InstructorAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public LessonService(LessonRepository lessonRepository, InstructorAvailabilityRepository availabilityRepository, UserRepository userRepository) {
        this.lessonRepository = lessonRepository;
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    // Create a lesson request (PENDING)
    @Transactional
    public Lesson createLesson(Long studentId, Long proId, OffsetDateTime startsAt, OffsetDateTime endsAt, Double price) {
        // basic validation
        if (startsAt == null || endsAt == null || !startsAt.isBefore(endsAt)) {
            throw new IllegalArgumentException("Invalid start/end");
        }
        if (studentId.equals(proId)) throw new IllegalArgumentException("Student and pro cannot be the same");

        User student = userRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("student not found"));
        User pro = userRepository.findById(proId).orElseThrow(() -> new IllegalArgumentException("pro not found"));

        // check for availability blocks
        List<?> blocks = availabilityRepository.findOverlapping(proId, startsAt, endsAt);
        if (!blocks.isEmpty()) {
            throw new IllegalStateException("Instructor has blocked availability during requested time");
        }

        // check accepted lessons overlap
        List<Lesson> conflicts = lessonRepository.findAcceptedOverlapping(proId, startsAt, endsAt);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Instructor already has an accepted lesson during requested time");
        }

        Lesson l = new Lesson();
        l.setStudent(student);
        l.setPro(pro);
        l.setStartsAt(startsAt);
        l.setEndsAt(endsAt);
        l.setPrice(price);
        l.setStatus(Lesson.LessonStatus.PENDING);

        return lessonRepository.save(l);
    }

    public Optional<Lesson> getLesson(Long id) {
        return lessonRepository.findById(id);
    }

    public List<Lesson> listForPro(Long proId, OffsetDateTime from, OffsetDateTime to) {
        return lessonRepository.findByProIdAndStartsAtBetween(proId, from, to);
    }

    // Accept a lesson - transactional and checks for conflicts
    @Transactional
    public Lesson acceptLesson(Long instructorId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new IllegalArgumentException("lesson not found"));
        if (!lesson.getPro().getId().equals(instructorId)) throw new SecurityException("not authorized");
        if (lesson.getStatus() != Lesson.LessonStatus.PENDING) throw new IllegalStateException("lesson not pending");

        // re-check conflicts
        List<Lesson> conflicts = lessonRepository.findAcceptedOverlapping(instructorId, lesson.getStartsAt(), lesson.getEndsAt());
        if (!conflicts.isEmpty()) throw new IllegalStateException("conflict with another accepted lesson");

        lesson.setStatus(Lesson.LessonStatus.ACCEPTED);
        lesson.setAcceptedAt(OffsetDateTime.now());
        return lessonRepository.save(lesson);
    }

    // Cancel lesson by either student or instructor
    @Transactional
    public Lesson cancelLesson(Long requesterId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new IllegalArgumentException("lesson not found"));
        // basic permission: student or pro can cancel; admin roles not checked here
        boolean isStudent = lesson.getStudent() != null && lesson.getStudent().getId().equals(requesterId);
        boolean isPro = lesson.getPro() != null && lesson.getPro().getId().equals(requesterId);
        if (!isStudent && !isPro) throw new SecurityException("not authorized to cancel");

        // compute simple cancellation fee policy: if cancel within 24h -> no refund (here we just mark fee)
        OffsetDateTime now = OffsetDateTime.now();
        long fee = 0L;
        if (lesson.getStartsAt() != null) {
            long hours = java.time.Duration.between(now, lesson.getStartsAt()).toHours();
            if (hours < 24) fee = Math.round((lesson.getPrice() == null ? 0.0 : lesson.getPrice()) * 100); // charge full
            else if (hours < 48) fee = Math.round((lesson.getPrice() == null ? 0.0 : lesson.getPrice()) * 100 * 0.5); // 50%
            else fee = 0L; // full refund
        }
        lesson.setCancellationFeeCents(fee);
        lesson.setStatus(Lesson.LessonStatus.CANCELLED);
        lesson.setCancelledAt(OffsetDateTime.now());
        return lessonRepository.save(lesson);
    }
}

