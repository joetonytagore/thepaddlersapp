package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thepaddlers.model.InstructorAvailability;

import java.time.OffsetDateTime;
import java.util.List;

public interface InstructorAvailabilityRepository extends JpaRepository<InstructorAvailability, Long> {
    @Query("select a from InstructorAvailability a where a.instructor.id = :instructorId and a.startAt < :end and a.endAt > :start")
    List<InstructorAvailability> findOverlapping(@Param("instructorId") Long instructorId, @Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    List<InstructorAvailability> findByInstructorIdAndStartAtBetween(Long instructorId, OffsetDateTime from, OffsetDateTime to);
}

