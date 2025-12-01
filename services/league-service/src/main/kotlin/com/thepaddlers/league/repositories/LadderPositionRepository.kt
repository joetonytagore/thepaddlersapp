package com.thepaddlers.league.repositories

import com.thepaddlers.league.entities.LadderPosition
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LadderPositionRepository : JpaRepository<LadderPosition, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<LadderPosition>
}
