package com.thepaddlers.league.services

import com.thepaddlers.league.entities.Match
import com.thepaddlers.league.entities.Score
import com.thepaddlers.league.repositories.MatchRepository
import com.thepaddlers.league.repositories.ScoreRepository
import java.util.*

// Standings row DTO
 data class StandingRow(
    val playerId: UUID,
    val matchesPlayed: Int,
    val wins: Int,
    val winPercentage: Double,
    val pointsScored: Int,
    val pointsPossible: Int,
    val pointPercentage: Double
)

class StandingsService(
    private val matchRepository: MatchRepository,
    private val scoreRepository: ScoreRepository
) {
    fun calculateWinPercentageStandings(leagueId: UUID): List<StandingRow> {
        val matches = matchRepository.findByLeagueId(leagueId)
        val scores = scoreRepository.findByMatchLeagueId(leagueId)
        val playerStats = mutableMapOf<UUID, StandingRow>()
        val playerIds = matches.flatMap { listOf(it.player1Id, it.player2Id) }.distinct()
        for (pid in playerIds) {
            val played = matches.count { it.player1Id == pid || it.player2Id == pid }
            val wins = matches.count { it.winnerId == pid }
            val scored = scores.filter { it.playerId == pid }.sumOf { it.score }
            val possible = scores.filter { it.match.player1Id == pid || it.match.player2Id == pid }.sumOf { it.score }
            val winPct = if (played > 0) wins.toDouble() / played else 0.0
            val pointPct = if (possible > 0) scored.toDouble() / possible else 0.0
            playerStats[pid] = StandingRow(pid, played, wins, winPct, scored, possible, pointPct)
        }
        return playerStats.values.sortedWith(compareByDescending<StandingRow> { it.winPercentage }.thenByDescending { it.wins })
    }

    fun calculatePointPercentageStandings(leagueId: UUID): List<StandingRow> {
        val matches = matchRepository.findByLeagueId(leagueId)
        val scores = scoreRepository.findByMatchLeagueId(leagueId)
        val playerStats = mutableMapOf<UUID, StandingRow>()
        val playerIds = matches.flatMap { listOf(it.player1Id, it.player2Id) }.distinct()
        for (pid in playerIds) {
            val played = matches.count { it.player1Id == pid || it.player2Id == pid }
            val wins = matches.count { it.winnerId == pid }
            val scored = scores.filter { it.playerId == pid }.sumOf { it.score }
            val possible = scores.filter { it.match.player1Id == pid || it.match.player2Id == pid }.sumOf { it.score }
            val winPct = if (played > 0) wins.toDouble() / played else 0.0
            val pointPct = if (possible > 0) scored.toDouble() / possible else 0.0
            playerStats[pid] = StandingRow(pid, played, wins, winPct, scored, possible, pointPct)
        }
        return playerStats.values.sortedWith(compareByDescending<StandingRow> { it.pointPercentage }.thenByDescending { it.pointsScored })
    }
}

