package com.thepaddlers.league.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "notification_logs")
data class NotificationLog(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(nullable = false) val orgId: UUID,
    @Column val userId: UUID?,
    @Column(nullable = false) val type: String,
    @Column val targetId: UUID?,
    @Column(nullable = false) val sentAt: Instant,
    @Column(nullable = false) val status: String,
    @Column val message: String?
)

