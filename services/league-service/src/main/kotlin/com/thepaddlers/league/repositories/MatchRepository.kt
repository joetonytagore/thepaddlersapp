package com.thepaddlers.league.repositories

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import com.thepaddlers.league.entities.Match

interface MatchRepository : JpaRepository<Match, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<Match>
    fun findByLeagueId(leagueId: UUID): List<Match>
}
