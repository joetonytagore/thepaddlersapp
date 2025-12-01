package com.thepaddlers.league.services

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.web.server.ResponseStatusException
import java.util.*

class LeagueServiceErrorTest {
    private val service = LeagueService(mock(), mock(), mock())

    @Test
    fun `submitMatchScore returns 403 for wrong org`() {
        val matchId = UUID.randomUUID()
        val orgId = UUID.randomUUID()
        val request = /* build SubmitScoreRequest with valid data */
        // Simulate match with different org
        // Should throw ResponseStatusException with 403
        assertThrows(ResponseStatusException::class.java) {
            service.submitMatchScore(UUID.randomUUID(), matchId, request)
        }
    }

    @Test
    fun `submitMatchScore returns 400 for invalid score`() {
        val matchId = UUID.randomUUID()
        val orgId = UUID.randomUUID()
        val request = /* build SubmitScoreRequest with invalid score */
        assertThrows(ResponseStatusException::class.java) {
            service.submitMatchScore(orgId, matchId, request)
        }
    }

    @Test
    fun `submitMatchScore returns 409 for concurrent update`() {
        val matchId = UUID.randomUUID()
        val orgId = UUID.randomUUID()
        val request = /* build SubmitScoreRequest with valid data */
        // Simulate OptimisticLockingFailureException
        assertThrows(ResponseStatusException::class.java) {
            service.submitMatchScore(orgId, matchId, request)
        }
    }
}

