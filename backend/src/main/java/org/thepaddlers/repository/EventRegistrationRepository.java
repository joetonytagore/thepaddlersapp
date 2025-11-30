package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.EventRegistration;
import org.thepaddlers.model.Event;
import org.thepaddlers.model.User;

import java.util.List;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    List<EventRegistration> findByEventAndStatus(Event event, EventRegistration.Status status);
    List<EventRegistration> findByEventOrderByWaitlistPositionAsc(Event event);
    List<EventRegistration> findByUser(User user);
    long countByEventAndStatus(Event event, EventRegistration.Status status);
}

