package com.thepaddlers.audit

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "audit_log")
data class AuditEntry(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(nullable = false) val actorId: UUID,
    @Column(nullable = false) val action: String,
    @Column val targetId: UUID?,
    @Column(nullable = false) val orgId: UUID,
    @Column(columnDefinition = "jsonb", nullable = false) val details: String,
    @Column(nullable = false) val createdAt: Instant = Instant.now()
)

