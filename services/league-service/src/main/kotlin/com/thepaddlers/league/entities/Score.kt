package com.thepaddlers.league.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "scores")
data class Score(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val organizationId: UUID,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    val match: Match,
    @Column(nullable = false)
    val playerId: UUID,
    @Column(nullable = false)
    val score: Int,
    @Column(nullable = false)
    val submittedAt: Instant = Instant.now()
)
