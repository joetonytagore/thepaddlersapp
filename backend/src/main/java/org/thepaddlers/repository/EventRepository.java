package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}

