package com.thepaddlers.league.repositories

import com.thepaddlers.league.entities.TournamentEntry
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TournamentEntryRepository : JpaRepository<TournamentEntry, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<TournamentEntry>
}
