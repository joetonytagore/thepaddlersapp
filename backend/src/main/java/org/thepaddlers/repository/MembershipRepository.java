package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.Membership;

import java.util.List;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByUserId(Long userId);
}

