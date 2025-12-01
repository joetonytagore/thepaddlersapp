package com.thepaddlers.league.repositories

import com.thepaddlers.league.entities.LeagueGroup
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LeagueGroupRepository : JpaRepository<LeagueGroup, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<LeagueGroup>
}
