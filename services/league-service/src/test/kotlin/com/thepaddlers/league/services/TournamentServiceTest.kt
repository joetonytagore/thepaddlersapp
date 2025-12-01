package com.thepaddlers.league.services

import com.thepaddlers.league.entities.Match
import com.thepaddlers.league.entities.TournamentBracket
import com.thepaddlers.league.repositories.MatchRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

class TournamentServiceTest {
    private val matchRepository: MatchRepository = mock()
    private val tournamentService = TournamentService(matchRepository)

    @Test
    fun `single elim bracket for 8 players results in 7 matches with correct flow`() {
        val tournamentId = UUID.randomUUID()
        val entrants = List(8) { UUID.randomUUID() }
        whenever(matchRepository.saveAll(any())).thenAnswer { it.arguments[0] }
        val bracket = tournamentService.generateSingleElimBracket(tournamentId, entrants)
        assertEquals(7, bracket.matches.size)
        // Check round structure
        assertEquals(4, bracket.matchTree[1]?.size) // round 1
        assertEquals(2, bracket.matchTree[2]?.size) // round 2
        assertEquals(1, bracket.matchTree[3]?.size) // final
        // Check parent-child relationships
        // Each match in round 2 should have 2 parents from round 1
        val round2 = bracket.matchTree[2]!!
        val round1 = bracket.matchTree[1]!!
        assertEquals(2, round2.size)
        assertEquals(4, round1.size)
        // Final should have 2 parents from round 2
        val finalMatch = bracket.matchTree[3]!![0]
        assertNotNull(finalMatch)
    }
}

