package com.thepaddlers.league.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "tournament_entries")
data class TournamentEntry(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val organizationId: UUID,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    val tournament: Tournament,
    @Column(nullable = false)
    val playerId: UUID,
    val joinedAt: Instant = Instant.now()
)

