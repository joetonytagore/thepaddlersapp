package org.thepaddlers.service;

import org.springframework.stereotype.Service;
import org.thepaddlers.model.AuditLog;
import org.thepaddlers.repository.AuditLogRepository;

@Service
public class AuditService {
    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    public AuditLog record(AuditLog audit) {
        return repo.save(audit);
    }
}

