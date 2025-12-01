package com.thepaddlers.league.entities
)
    val endTime: Instant? = null
    val startTime: Instant? = null,
    val name: String,
    @Column(nullable = false)
    val league: League,
    @JoinColumn(name = "league_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    val organizationId: UUID,
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Id
data class Tournament(
@Table(name = "tournaments")
@Entity

import java.util.*
import java.time.Instant
import jakarta.persistence.*


