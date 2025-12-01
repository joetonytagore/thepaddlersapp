package com.thepaddlers.league.repositories

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import com.thepaddlers.league.entities.Checkin

interface CheckinRepository : JpaRepository<Checkin, UUID> {
    fun findByOrganizationId(organizationId: UUID): List<Checkin>
}
