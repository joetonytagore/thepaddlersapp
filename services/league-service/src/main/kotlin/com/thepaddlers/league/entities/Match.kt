package com.thepaddlers.league.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "matches")
data class Match(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val organizationId: UUID,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    val league: League? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    val group: LeagueGroup? = null,
    @Column(nullable = false)
    val player1Id: UUID,
    @Column(nullable = false)
    val player2Id: UUID,
    val scheduledTime: Instant? = null,
    val completedTime: Instant? = null,
    @Column(nullable = false)
    val status: String,
    val winnerId: UUID? = null,
    val createdAt: Instant = Instant.now()
)
