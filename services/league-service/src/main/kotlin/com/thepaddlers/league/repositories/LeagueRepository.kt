package com.thepaddlers.league.repositories

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import com.thepaddlers.league.entities.League

interface LeagueRepository : JpaRepository<League, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<League>
}
