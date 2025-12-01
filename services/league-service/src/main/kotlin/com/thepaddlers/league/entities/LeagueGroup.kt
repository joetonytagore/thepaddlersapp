package com.thepaddlers.league.entities
)
    val name: String
    @Column(nullable = false)
    val league: League,
    @JoinColumn(name = "league_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    val organizationId: UUID,
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),
    @Id
data class LeagueGroup(
@Table(name = "league_groups")
@Entity

import java.util.*
import jakarta.persistence.*


