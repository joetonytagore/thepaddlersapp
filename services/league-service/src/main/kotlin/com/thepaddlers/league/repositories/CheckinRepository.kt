package com.thepaddlers.league.repositories

}
    fun findByOrganizationId(organizationId: UUID): List<Checkin>
interface CheckinRepository : JpaRepository<Checkin, UUID> {

import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import com.thepaddlers.league.entities.Checkin
