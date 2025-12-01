package com.thepaddlers.league.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.*

enum class QueueableType { LEAGUE_EVENT, TOURNAMENT, MATCH }

@Entity
@Table(name = "waitlist_entries")
data class WaitlistEntry(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(nullable = false) val organizationId: UUID,
    @Enumerated(EnumType.STRING) @Column(nullable = false) val queueableType: QueueableType,
    @Column(nullable = false) val queueableId: UUID,
    @Column(nullable = false) val userId: UUID,
    @Column(nullable = false) val createdAt: Instant = Instant.now(),
    @Column val offerExpiresAt: Instant? = null,
    @Column val offerAccepted: Boolean = false
)

