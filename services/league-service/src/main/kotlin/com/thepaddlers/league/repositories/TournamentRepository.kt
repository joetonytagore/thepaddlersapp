package com.thepaddlers.league.repositories

import com.thepaddlers.league.entities.Tournament
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TournamentRepository : JpaRepository<Tournament, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<Tournament>
}
