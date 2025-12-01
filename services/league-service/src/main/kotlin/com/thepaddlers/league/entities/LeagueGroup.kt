package com.thepaddlers.league.entities

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "league_groups")
data class LeagueGroup(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val organizationId: UUID,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    val league: League,
    @Column(nullable = false)
    val name: String
)
