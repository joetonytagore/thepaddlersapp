package com.thepaddlers.league.repositories

}
    fun findByOrganizationId(organizationId: UUID): List<Match>
interface MatchRepository : JpaRepository<Match, UUID> {

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import com.thepaddlers.league.entities.Match
