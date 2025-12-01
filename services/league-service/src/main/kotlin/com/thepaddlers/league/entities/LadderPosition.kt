package com.thepaddlers.league.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "ladder_positions")
data class LadderPosition(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val organizationId: UUID,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    val league: League,
    @Column(nullable = false)
    val playerId: UUID,
    val position: Int,
    val updatedAt: Instant = Instant.now()
)

