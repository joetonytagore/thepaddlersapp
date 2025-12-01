package com.thepaddlers.league.services

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class LadderServiceTest {
    private val ladderService = LadderService()

    @Test
    fun `5 players results in 2 matches and 1 bye`() {
        val leagueId = UUID.randomUUID()
        val players = List(5) { UUID.randomUUID() }
        val pairings = ladderService.generateLadderPairings(leagueId, players)
        // Should be 2 matches (pairs) and 1 bye (top player)
        assertEquals(2, pairings.size)
        val pairedPlayers = pairings.flatMap { listOf(it.first, it.second) }
        // Top player should not be in any pair
        assertFalse(pairedPlayers.contains(players[0]))
        // All other players should be paired
        assertTrue(pairedPlayers.containsAll(players.drop(1)))
    }
}

