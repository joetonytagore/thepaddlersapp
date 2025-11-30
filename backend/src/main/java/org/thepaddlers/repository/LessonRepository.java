package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thepaddlers.model.Lesson;

import java.time.OffsetDateTime;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    // Find lessons for a pro that overlap a given interval (end-exclusive).
    @Query("select l from Lesson l where l.pro.id = :proId and l.status = 'ACCEPTED' and l.startsAt < :end and l.endsAt > :start")
    List<Lesson> findAcceptedOverlapping(@Param("proId") Long proId, @Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    List<Lesson> findByStudentIdAndStartsAtBetween(Long studentId, OffsetDateTime from, OffsetDateTime to);
    List<Lesson> findByProIdAndStartsAtBetween(Long proId, OffsetDateTime from, OffsetDateTime to);
}

