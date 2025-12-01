package com.thepaddlers.league.repositories

import com.thepaddlers.league.entities.WaitlistEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WaitlistEntryRepository : JpaRepository<WaitlistEntry, UUID> {
    fun findByOrganizationIdAndQueueableTypeAndQueueableIdAndOfferAcceptedFalseOrderByCreatedAtAsc(
        organizationId: UUID,
        queueableType: com.thepaddlers.league.entities.QueueableType,
        queueableId: UUID
    ): List<WaitlistEntry>
}

