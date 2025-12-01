package com.thepaddlers.league.services

import com.thepaddlers.league.dto.JoinWaitlistRequest
import com.thepaddlers.league.entities.QueueableType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@Testcontainers
class WaitlistConcurrencyIntegrationTest @Autowired constructor(
    private val waitlistService: WaitlistService
) {
    @Test
    fun `concurrent waitlist join and spot open assigns only one`() {
        val orgId = UUID.randomUUID()
        val queueableId = UUID.randomUUID()
        val userIds = List(30) { UUID.randomUUID() }
        val latch = CountDownLatch(30)
        val executor = Executors.newFixedThreadPool(10)
        userIds.forEach { userId ->
            executor.submit {
                waitlistService.joinWaitlist(orgId, JoinWaitlistRequest(QueueableType.MATCH, queueableId, userId))
                latch.countDown()
            }
        }
        latch.await(10, TimeUnit.SECONDS)
        // Simulate cancellation/spot open
        waitlistService.handleSpotOpen(orgId, QueueableType.MATCH, queueableId)
        // Only one user should have offerExpiresAt set
        val waitlist = waitlistService.getWaitlistEntries(orgId, QueueableType.MATCH, queueableId)
        val offers = waitlist.count { it.offerExpiresAt != null }
        assertEquals(1, offers)
    }
}

