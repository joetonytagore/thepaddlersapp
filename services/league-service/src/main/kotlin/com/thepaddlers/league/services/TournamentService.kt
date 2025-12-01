package com.thepaddlers.league.services

import com.thepaddlers.league.entities.Match
import com.thepaddlers.league.entities.Tournament
import com.thepaddlers.league.entities.TournamentBracket
import com.thepaddlers.league.repositories.MatchRepository
import com.thepaddlers.league.repositories.TournamentBracketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TournamentService(
    private val matchRepository: MatchRepository,
    private val tournamentBracketRepository: TournamentBracketRepository
) {
    @Transactional
    fun generateSingleElimBracket(
        organizationId: UUID,
        tournament: Tournament,
        entrants: List<UUID>,
        seedList: List<UUID>? = null
    ) {
        val seeds = seedList ?: entrants.shuffled()
        val numPlayers = seeds.size
        val numRounds = Math.ceil(Math.log(numPlayers.toDouble()) / Math.log(2.0)).toInt()
        val matches = mutableListOf<Match>()
        // Round 1
        for (i in 0 until numPlayers / 2) {
            val m = Match(
                id = UUID.randomUUID(),
                organizationId = organizationId,
                league = null,
                group = null,
                player1Id = seeds[i * 2],
                player2Id = seeds[i * 2 + 1],
                scheduledTime = null,
                completedTime = null,
                status = "SCHEDULED",
                winnerId = null,
                createdAt = java.time.Instant.now()
            )
            matches.add(m)
            matchRepository.save(m)
            val bracket = TournamentBracket(
                organizationId = organizationId,
                tournament = tournament,
                round = 1,
                match = m
            )
            tournamentBracketRepository.save(bracket)
        }
        // Future rounds
        var prevRoundMatches = matches
        for (r in 2..numRounds) {
            val thisRoundMatches = mutableListOf<Match>()
            for (i in 0 until prevRoundMatches.size / 2) {
                val m = Match(
                    id = UUID.randomUUID(),
                    organizationId = organizationId,
                    league = null,
                    group = null,
                    player1Id = UUID.randomUUID(),
                    player2Id = UUID.randomUUID(),
                    scheduledTime = null,
                    completedTime = null,
                    status = "PENDING",
                    winnerId = null,
                    createdAt = java.time.Instant.now()
                )
                thisRoundMatches.add(m)
                matchRepository.save(m)
                val bracket = TournamentBracket(
                    organizationId = organizationId,
                    tournament = tournament,
                    round = r,
                    match = m
                )
                tournamentBracketRepository.save(bracket)
            }
            prevRoundMatches = thisRoundMatches
        }
    }

    fun generateDoubleElimBracket(/* params */): TournamentBracket {
        // TODO: Implement double-elimination bracket generation
        // Winners and losers brackets, match flow, etc.
        throw NotImplementedError("Double elimination bracket not implemented yet")
    }
}
