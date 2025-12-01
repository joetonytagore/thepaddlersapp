package com.thepaddlers.league.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "league_players")
data class LeaguePlayer(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val organizationId: UUID,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    val league: League,
    @Column(nullable = false)
    val playerId: UUID,
    val joinedAt: Instant = Instant.now()
)

