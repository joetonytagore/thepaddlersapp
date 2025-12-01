package com.thepaddlers.league.repositories

import com.thepaddlers.league.entities.LeaguePlayer
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LeaguePlayerRepository : JpaRepository<LeaguePlayer, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<LeaguePlayer>
}
