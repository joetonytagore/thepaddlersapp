package com.thepaddlers.league.entities

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "tournament_brackets")
data class TournamentBracket(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val organizationId: UUID,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    val tournament: Tournament,
    val round: Int,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    val match: Match
)

