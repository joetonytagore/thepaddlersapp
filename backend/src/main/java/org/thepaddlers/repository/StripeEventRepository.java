package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.StripeEvent;

import java.util.Optional;

public interface StripeEventRepository extends JpaRepository<StripeEvent, Long> {
    Optional<StripeEvent> findByEventId(String eventId);
}

