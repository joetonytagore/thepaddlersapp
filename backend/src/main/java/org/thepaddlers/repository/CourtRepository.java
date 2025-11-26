package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.Court;

public interface CourtRepository extends JpaRepository<Court, Long> {
}

