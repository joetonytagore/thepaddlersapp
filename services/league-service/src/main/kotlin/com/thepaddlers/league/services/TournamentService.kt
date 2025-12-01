package com.thepaddlers.league.services

import com.thepaddlers.league.entities.Match
import com.thepaddlers.league.entities.TournamentBracket
import com.thepaddlers.league.repositories.MatchRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TournamentService(
    private val matchRepository: MatchRepository
) {
    @Transactional
    fun generateSingleElimBracket(
        tournamentId: UUID,
        entrants: List<UUID>,
        seedList: List<UUID>? = null
    ): TournamentBracket {
        val seeds = seedList ?: entrants.shuffled()
        val numPlayers = seeds.size
        val numRounds = Math.ceil(Math.log(numPlayers.toDouble()) / Math.log(2.0)).toInt()
        val numMatches = numPlayers - 1
        val matches = mutableListOf<Match>()
        val matchTree = mutableMapOf<Int, MutableList<Match>>()
        // Round 1
        val round1 = mutableListOf<Match>()
        for (i in 0 until numPlayers / 2) {
            val m = Match(
                id = UUID.randomUUID(),
                organizationId = UUID.randomUUID(), // TODO: pass orgId if needed
                league = null, // Not used for tournament
                group = null,
                player1Id = seeds[i * 2],
                player2Id = seeds[i * 2 + 1],
                scheduledTime = null,
                completedTime = null,
                status = "SCHEDULED",
                winnerId = null,
                createdAt = java.time.Instant.now()
            )
            round1.add(m)
            matches.add(m)
        }
        matchTree[1] = round1
        // Future rounds
        var prevRound = round1
        for (r in 2..numRounds) {
            val thisRound = mutableListOf<Match>()
            for (i in 0 until prevRound.size / 2) {
                val m = Match(
                    id = UUID.randomUUID(),
                    organizationId = UUID.randomUUID(), // TODO: pass orgId if needed
                    league = null,
                    group = null,
                    player1Id = UUID.randomUUID(), // Placeholder, will be winner of previous
                    player2Id = UUID.randomUUID(), // Placeholder
                    scheduledTime = null,
                    completedTime = null,
                    status = "PENDING",
                    winnerId = null,
                    createdAt = java.time.Instant.now()
                )
                thisRound.add(m)
                matches.add(m)
            }
            matchTree[r] = thisRound
            prevRound = thisRound
        }
        matchRepository.saveAll(matches)
        return TournamentBracket(
            tournamentId = tournamentId,
            matches = matches,
            matchTree = matchTree
        )
    }

    fun generateDoubleElimBracket(/* params */): TournamentBracket {
        // TODO: Implement double-elimination bracket generation
        // Winners and losers brackets, match flow, etc.
        throw NotImplementedError("Double elimination bracket not implemented yet")
    }
}

