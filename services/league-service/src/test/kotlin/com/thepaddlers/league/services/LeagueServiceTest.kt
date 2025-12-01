package com.thepaddlers.league.services

import com.thepaddlers.league.dto.ScoreEntry
import com.thepaddlers.league.dto.SubmitScoreRequest
import com.thepaddlers.league.entities.Match
import com.thepaddlers.league.entities.Score
import com.thepaddlers.league.repositories.MatchRepository
import com.thepaddlers.league.repositories.ScoreRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.OptimisticLockingFailureException
import java.time.Instant
import java.util.*

class LeagueServiceTest {
    private val matchRepository: MatchRepository = mock()
    private val scoreRepository: ScoreRepository = mock()
    private val eventPublisher: ApplicationEventPublisher = mock()
    private val leagueService = LeagueService(matchRepository, scoreRepository, eventPublisher)

    @Test
    fun `invalid submitter throws`() {
        val matchId = UUID.randomUUID()
        val orgId = UUID.randomUUID()
        val match = Match(
            id = matchId,
            organizationId = orgId,
            league = mock(),
            group = null,
            player1Id = UUID.randomUUID(),
            player2Id = UUID.randomUUID(),
            scheduledTime = Instant.now(),
            completedTime = null,
            status = "SCHEDULED",
            winnerId = null,
            createdAt = Instant.now()
        )
        whenever(matchRepository.findById(matchId)).thenReturn(Optional.of(match))
        val request = SubmitScoreRequest(
            submittedByUserId = UUID.randomUUID(), // not a player
            score = listOf(ScoreEntry(match.player1Id, 21), ScoreEntry(match.player2Id, 15)),
            notes = null
        )
        assertThrows(IllegalArgumentException::class.java) {
            leagueService.submitMatchScore(orgId, matchId, request)
        }
    }

    @Test
    fun `invalid score format throws`() {
        val matchId = UUID.randomUUID()
        val orgId = UUID.randomUUID()
        val match = Match(
            id = matchId,
            organizationId = orgId,
            league = mock(),
            group = null,
            player1Id = UUID.randomUUID(),
            player2Id = UUID.randomUUID(),
            scheduledTime = Instant.now(),
            completedTime = null,
            status = "SCHEDULED",
            winnerId = null,
            createdAt = Instant.now()
        )
        whenever(matchRepository.findById(matchId)).thenReturn(Optional.of(match))
        val request = SubmitScoreRequest(
            submittedByUserId = match.player1Id,
            score = listOf(ScoreEntry(match.player1Id, 10), ScoreEntry(match.player2Id, 10)), // tie
            notes = null
        )
        assertThrows(IllegalArgumentException::class.java) {
            leagueService.submitMatchScore(orgId, matchId, request)
        }
    }

    @Test
    fun `successful submission saves scores, updates match, publishes event`() {
        val matchId = UUID.randomUUID()
        val orgId = UUID.randomUUID()
        val match = Match(
            id = matchId,
            organizationId = orgId,
            league = mock(),
            group = null,
            player1Id = UUID.randomUUID(),
            player2Id = UUID.randomUUID(),
            scheduledTime = Instant.now(),
            completedTime = null,
            status = "SCHEDULED",
            winnerId = null,
            createdAt = Instant.now()
        )
        whenever(matchRepository.findById(matchId)).thenReturn(Optional.of(match))
        whenever(matchRepository.save(any())).thenReturn(match.copy(status = "COMPLETED", completedTime = Instant.now(), winnerId = match.player1Id))
        val request = SubmitScoreRequest(
            submittedByUserId = match.player1Id,
            score = listOf(ScoreEntry(match.player1Id, 21), ScoreEntry(match.player2Id, 15)),
            notes = "Good match"
        )
        leagueService.submitMatchScore(orgId, matchId, request)
        verify(scoreRepository).save(any())
        verify(matchRepository).save(any())
        verify(eventPublisher).publishEvent(any())
    }
}

