package com.thepaddlers.league.repositories

import com.thepaddlers.league.entities.NotificationLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NotificationLogRepository : JpaRepository<NotificationLog, UUID> {
    fun findByOrgId(orgId: UUID): List<NotificationLog>
}

