package org.thepaddlers.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.AuditLog;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByOrgIdOrderByCreatedAtDesc(Long orgId);

    Page<AuditLog> findByOrgIdOrderByCreatedAtDesc(Long orgId, Pageable pageable);
}
