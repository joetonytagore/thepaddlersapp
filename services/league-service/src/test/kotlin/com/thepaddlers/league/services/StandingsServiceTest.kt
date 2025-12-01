package com.thepaddlers.league.services

import com.thepaddlers.league.entities.Match
import com.thepaddlers.league.entities.Score
import com.thepaddlers.league.repositories.MatchRepository
import com.thepaddlers.league.repositories.ScoreRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

class StandingsServiceTest {
    private val matchRepository: MatchRepository = mock()
    private val scoreRepository: ScoreRepository = mock()
    private val standingsService = StandingsService(matchRepository, scoreRepository)

    @Test
    fun `win percentage standings for 3 players round-robin`() {
        val leagueId = UUID.randomUUID()
        val p1 = UUID.randomUUID()
        val p2 = UUID.randomUUID()
        val p3 = UUID.randomUUID()
        val matches = listOf(
            Match(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), league = mock(), group = null, player1Id = p1, player2Id = p2, scheduledTime = null, completedTime = null, status = "COMPLETED", winnerId = p1, createdAt = java.time.Instant.now()),
            Match(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), league = mock(), group = null, player1Id = p2, player2Id = p3, scheduledTime = null, completedTime = null, status = "COMPLETED", winnerId = p3, createdAt = java.time.Instant.now()),
            Match(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), league = mock(), group = null, player1Id = p1, player2Id = p3, scheduledTime = null, completedTime = null, status = "COMPLETED", winnerId = p1, createdAt = java.time.Instant.now())
        )
        val scores = listOf(
            Score(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), match = matches[0], playerId = p1, score = 21, submittedAt = java.time.Instant.now()),
            Score(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), match = matches[0], playerId = p2, score = 15, submittedAt = java.time.Instant.now()),
            Score(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), match = matches[1], playerId = p2, score = 18, submittedAt = java.time.Instant.now()),
            Score(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), match = matches[1], playerId = p3, score = 21, submittedAt = java.time.Instant.now()),
            Score(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), match = matches[2], playerId = p1, score = 21, submittedAt = java.time.Instant.now()),
            Score(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), match = matches[2], playerId = p3, score = 19, submittedAt = java.time.Instant.now())
        )
        whenever(matchRepository.findByLeagueId(leagueId)).thenReturn(matches)
        whenever(scoreRepository.findByMatchLeagueId(leagueId)).thenReturn(scores)
        val standings = standingsService.calculateWinPercentageStandings(leagueId)
        assertEquals(3, standings.size)
        assertEquals(standings[0].playerId, p1) // p1 has 2 wins
        assertEquals(standings[1].playerId, p3) // p3 has 1 win
        assertEquals(standings[2].playerId, p2) // p2 has 0 wins
    }

    @Test
    fun `point percentage standings for 3 players round-robin`() {
        val leagueId = UUID.randomUUID()
        val p1 = UUID.randomUUID()
        val p2 = UUID.randomUUID()
        val p3 = UUID.randomUUID()
        val matches = listOf(
            Match(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), league = mock(), group = null, player1Id = p1, player2Id = p2, scheduledTime = null, completedTime = null, status = "COMPLETED", winnerId = p1, createdAt = java.time.Instant.now()),
            Match(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), league = mock(), group = null, player1Id = p2, player2Id = p3, scheduledTime = null, completedTime = null, status = "COMPLETED", winnerId = p3, createdAt = java.time.Instant.now()),
            Match(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), league = mock(), group = null, player1Id = p1, player2Id = p3, scheduledTime = null, completedTime = null, status = "COMPLETED", winnerId = p1, createdAt = java.time.Instant.now())
        )
        val scores = listOf(
            Score(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), match = matches[0], playerId = p1, score = 21, submittedAt = java.time.Instant.now()),
            Score(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), match = matches[0], playerId = p2, score = 15, submittedAt = java.time.Instant.now()),
            Score(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), match = matches[1], playerId = p2, score = 18, submittedAt = java.time.Instant.now()),
            Score(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), match = matches[1], playerId = p3, score = 21, submittedAt = java.time.Instant.now()),
            Score(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), match = matches[2], playerId = p1, score = 21, submittedAt = java.time.Instant.now()),
            Score(id = UUID.randomUUID(), organizationId = UUID.randomUUID(), match = matches[2], playerId = p3, score = 19, submittedAt = java.time.Instant.now())
        )
        whenever(matchRepository.findByLeagueId(leagueId)).thenReturn(matches)
        whenever(scoreRepository.findByMatchLeagueId(leagueId)).thenReturn(scores)
        val standings = standingsService.calculatePointPercentageStandings(leagueId)
        assertEquals(3, standings.size)
        assertEquals(standings[0].playerId, p1) // p1 has highest point percentage
        assertEquals(standings[1].playerId, p3)
        assertEquals(standings[2].playerId, p2)
    }
}

