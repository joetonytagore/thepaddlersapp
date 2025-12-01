package com.thepaddlers.league.services

import com.thepaddlers.league.dto.JoinWaitlistRequest
import com.thepaddlers.league.dto.AcceptOfferRequest
import com.thepaddlers.league.entities.QueueableType
import com.thepaddlers.league.entities.WaitlistEntry
import com.thepaddlers.league.repositories.WaitlistEntryRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class WaitlistService(
    private val waitlistEntryRepository: WaitlistEntryRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun joinWaitlist(orgId: UUID, request: JoinWaitlistRequest): WaitlistEntry {
        val entry = WaitlistEntry(
            id = UUID.randomUUID(),
            organizationId = orgId,
            queueableType = request.queueableType,
            queueableId = request.queueableId,
            userId = request.userId,
            createdAt = Instant.now()
        )
        return waitlistEntryRepository.save(entry)
    }

    @Transactional
    fun acceptOffer(orgId: UUID, entryId: UUID, request: AcceptOfferRequest) {
        val entry = waitlistEntryRepository.findById(entryId).orElseThrow { IllegalArgumentException("Waitlist entry not found") }
        if (entry.organizationId != orgId || entry.userId != request.userId) throw IllegalArgumentException("Invalid org or user")
        if (entry.offerExpiresAt == null || entry.offerExpiresAt.isBefore(Instant.now())) throw IllegalArgumentException("Offer expired")
        if (entry.offerAccepted) throw IllegalArgumentException("Offer already accepted")
        waitlistEntryRepository.save(entry.copy(offerAccepted = true))
        // TODO: Create reservation/entry for user in queueable entity
        // TODO: Send notification to user
    }

    @Transactional
    fun handleSpotOpen(orgId: UUID, queueableType: QueueableType, queueableId: UUID) {
        val waitlist = waitlistEntryRepository.findByOrganizationIdAndQueueableTypeAndQueueableIdAndOfferAcceptedFalseOrderByCreatedAtAsc(
            orgId, queueableType, queueableId
        )
        if (waitlist.isNotEmpty()) {
            val entry = waitlist.first()
            val offerExpiresAt = Instant.now().plusSeconds(600) // 10 min TTL
            waitlistEntryRepository.save(entry.copy(offerExpiresAt = offerExpiresAt))
            // TODO: Send notification to user
        }
    }

    @Transactional
    fun handleOfferExpiry(orgId: UUID, queueableType: QueueableType, queueableId: UUID) {
        val waitlist = waitlistEntryRepository.findByOrganizationIdAndQueueableTypeAndQueueableIdAndOfferAcceptedFalseOrderByCreatedAtAsc(
            orgId, queueableType, queueableId
        )
        val now = Instant.now()
        val expired = waitlist.filter { it.offerExpiresAt != null && it.offerExpiresAt.isBefore(now) }
        expired.forEach {
            // Move offer to next user
            waitlistEntryRepository.save(it.copy(offerExpiresAt = null))
            val next = waitlist.filter { it.createdAt.isAfter(it.createdAt) && !it.offerAccepted }.minByOrNull { it.createdAt }
            if (next != null) {
                val offerExpiresAt = now.plusSeconds(600)
                waitlistEntryRepository.save(next.copy(offerExpiresAt = offerExpiresAt))
                // TODO: Send notification to next user
            }
        }
    }
}

