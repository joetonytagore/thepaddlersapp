package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}

