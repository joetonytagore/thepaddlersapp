package com.thepaddlers.league.entities
)
    val submittedAt: Instant = Instant.now()
    val score: Int,
    val playerId: UUID,
    @Column(nullable = false)
    val match: Match,
    @JoinColumn(name = "match_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    val organizationId: UUID,
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Id
data class Score(
@Table(name = "scores")
@Entity

import java.util.*
import java.time.Instant
import jakarta.persistence.*


