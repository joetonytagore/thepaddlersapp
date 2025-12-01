package com.thepaddlers.league.repositories

}
    fun findByOrganizationId(organizationId: UUID): List<League>
interface LeagueRepository : JpaRepository<League, UUID> {

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import com.thepaddlers.league.entities.League
