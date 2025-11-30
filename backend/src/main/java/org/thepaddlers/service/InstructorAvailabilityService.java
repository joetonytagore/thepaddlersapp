package org.thepaddlers.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thepaddlers.model.InstructorAvailability;
import org.thepaddlers.model.Lesson;
import org.thepaddlers.model.User;
import org.thepaddlers.repository.InstructorAvailabilityRepository;
import org.thepaddlers.repository.LessonRepository;
import org.thepaddlers.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class InstructorAvailabilityService {
    private final InstructorAvailabilityRepository availabilityRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    public InstructorAvailabilityService(InstructorAvailabilityRepository availabilityRepository, LessonRepository lessonRepository, UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public InstructorAvailability createAvailability(Long instructorId, OffsetDateTime startAt, OffsetDateTime endAt, InstructorAvailability.AvailabilityType type, String notes) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) throw new IllegalArgumentException("invalid start/end");
        User instructor = userRepository.findById(instructorId).orElseThrow(() -> new IllegalArgumentException("instructor not found"));

        // For BLOCKED entries, ensure there are no accepted lessons overlapping
        if (type == InstructorAvailability.AvailabilityType.BLOCKED) {
            List<Lesson> conflicts = lessonRepository.findAcceptedOverlapping(instructorId, startAt, endAt);
            if (!conflicts.isEmpty()) throw new IllegalStateException("Cannot block time that has accepted lessons");
        }

        InstructorAvailability a = new InstructorAvailability();
        a.setInstructor(instructor);
        a.setStartAt(startAt);
        a.setEndAt(endAt);
        a.setType(type);
        a.setNotes(notes);
        return availabilityRepository.save(a);
    }

    @Transactional(readOnly = true)
    public List<InstructorAvailability> listAvailability(Long instructorId, OffsetDateTime from, OffsetDateTime to) {
        return availabilityRepository.findByInstructorIdAndStartAtBetween(instructorId, from, to);
    }

    @Transactional
    public void deleteAvailability(Long availabilityId) {
        availabilityRepository.deleteById(availabilityId);
    }
}

