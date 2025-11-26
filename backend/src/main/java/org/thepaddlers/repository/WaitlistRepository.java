package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.WaitlistEntry;

public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {
}

