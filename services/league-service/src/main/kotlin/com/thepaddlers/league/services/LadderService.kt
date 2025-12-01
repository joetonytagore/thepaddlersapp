package com.thepaddlers.league.services

import java.util.*

class LadderService {
    fun generateLadderPairings(leagueId: UUID, ladderPositions: List<UUID>): List<Pair<UUID, UUID>> {
        val pairings = mutableListOf<Pair<UUID, UUID>>()
        val count = ladderPositions.size
        var i = 0
        if (count % 2 == 1 && count > 0) {
            // Top player gets a bye
            i = 1
        }
        while (i < count - 1) {
            pairings.add(Pair(ladderPositions[i], ladderPositions[i + 1]))
            i += 2
        }
        // TODO: Implement promotion/demotion logic to update ladder_positions after results
        return pairings
    }
}

