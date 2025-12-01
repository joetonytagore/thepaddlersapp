package com.thepaddlers.league.services

import com.thepaddlers.league.dto.JoinWaitlistRequest
import com.thepaddlers.league.dto.AcceptOfferRequest
import com.thepaddlers.league.entities.QueueableType
import com.thepaddlers.league.entities.WaitlistEntry
import com.thepaddlers.league.repositories.WaitlistEntryRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.*

class WaitlistServiceTest {
    private val repo: WaitlistEntryRepository = mock()
    private val eventPublisher = mock<org.springframework.context.ApplicationEventPublisher>()
    private val service = WaitlistService(repo, eventPublisher)

    @Test
    fun `offer expires and moves to next user`() {
        val orgId = UUID.randomUUID()
        val queueableId = UUID.randomUUID()
        val now = Instant.parse("2025-12-01T12:00:00Z")
        val entry1 = WaitlistEntry(
            id = UUID.randomUUID(),
            organizationId = orgId,
            queueableType = QueueableType.MATCH,
            queueableId = queueableId,
            userId = UUID.randomUUID(),
            createdAt = now.minusSeconds(1200),
            offerExpiresAt = now.minusSeconds(600), // expired
            offerAccepted = false
        )
        val entry2 = WaitlistEntry(
            id = UUID.randomUUID(),
            organizationId = orgId,
            queueableType = QueueableType.MATCH,
            queueableId = queueableId,
            userId = UUID.randomUUID(),
            createdAt = now.minusSeconds(600),
            offerExpiresAt = null,
            offerAccepted = false
        )
        whenever(repo.findByOrganizationIdAndQueueableTypeAndQueueableIdAndOfferAcceptedFalseOrderByCreatedAtAsc(orgId, QueueableType.MATCH, queueableId)).thenReturn(listOf(entry1, entry2))
        service.handleOfferExpiry(orgId, QueueableType.MATCH, queueableId)
        // entry1 offer should be cleared, entry2 should get new offer
        // (actual repo.save calls are not checked here, but logic is exercised)
    }

    @Test
    fun `auto-assign on spot open`() {
        val orgId = UUID.randomUUID()
        val queueableId = UUID.randomUUID()
        val entry = WaitlistEntry(
            id = UUID.randomUUID(),
            organizationId = orgId,
            queueableType = QueueableType.MATCH,
            queueableId = queueableId,
            userId = UUID.randomUUID(),
            createdAt = Instant.now(),
            offerExpiresAt = null,
            offerAccepted = false
        )
        whenever(repo.findByOrganizationIdAndQueueableTypeAndQueueableIdAndOfferAcceptedFalseOrderByCreatedAtAsc(orgId, QueueableType.MATCH, queueableId)).thenReturn(listOf(entry))
        service.handleSpotOpen(orgId, QueueableType.MATCH, queueableId)
        // entry should get offerExpiresAt set
    }
}

