package com.thepaddlers.league.repositories

import com.thepaddlers.league.entities.TournamentBracket
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TournamentBracketRepository : JpaRepository<TournamentBracket, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<TournamentBracket>
}
