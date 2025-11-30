package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.BookingRule;
import org.thepaddlers.model.Court;

import java.util.List;

public interface BookingRuleRepository extends JpaRepository<BookingRule, Long> {
    List<BookingRule> findByCourt(Court court);
}
