package com.thepaddlers.league.repositories

import com.thepaddlers.league.entities.Score
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ScoreRepository : JpaRepository<Score, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<Score>
}
