package com.thepaddlers.league.services

import com.thepaddlers.league.dto.SubmitScoreRequest
import com.thepaddlers.league.dto.ScoreEntry
import com.thepaddlers.league.dto.MobileLeagueSummary
import com.thepaddlers.league.dto.MobileCheckinRequest
import com.thepaddlers.league.dto.MobileSubmitScoreRequest
import com.thepaddlers.league.entities.Match
import com.thepaddlers.league.entities.Score
import com.thepaddlers.league.repositories.MatchRepository
import com.thepaddlers.league.repositories.ScoreRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Instant
import java.util.*
import java.util.regex.Pattern

@Service
class LeagueService(
    private val matchRepository: MatchRepository,
    private val scoreRepository: ScoreRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun generateSchedule(leagueId: UUID, format: String) {
        // TODO: Implement round-robin and ladder scheduling logic
        // For round-robin: generate all possible matches between players
        // For ladder: implement ladder reshuffling rules
        // Server-side validation: org scoping, time windows, capacity checks
        val league = /* fetch league by leagueId */
        if (league == null) throw ResponseStatusException(HttpStatus.NOT_FOUND, "League not found")
        if (league.status != "ACTIVE") throw ResponseStatusException(HttpStatus.CONFLICT, "Schedule already generated")
        // TODO: Validate time windows and capacity
    }

    @Transactional
    fun enterLeague(leagueId: UUID, playerId: UUID) {
        val league = /* fetch league by leagueId */
        if (league == null) throw ResponseStatusException(HttpStatus.NOT_FOUND, "League not found")
        // TODO: Check org scoping
        // TODO: Capacity check
    }

    @Transactional
    fun submitScore(matchId: UUID, playerId: UUID, score: Int) {
        val match = matchRepository.findById(matchId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found") }
        // TODO: Check org scoping
        // TODO: Validate match, update score, handle completion
    }

    fun getStandings(leagueId: UUID): Any {
        // TODO: Calculate standings based on scores and matches
        return Any()
    }

    @Transactional
    fun submitMatchScore(orgId: UUID, matchId: UUID, request: SubmitScoreRequest) {
        val match = matchRepository.findById(matchId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found") }
        if (match.organizationId != orgId) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Match does not belong to org")
        val playerIds = listOf(match.player1Id, match.player2Id)
        if (!playerIds.contains(request.submittedByUserId)) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Submitter must be a player in the match")
        if (request.score.size != 2) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Score must have 2 entries")
        val points = request.score.map { it.points }
        val allowedRange = 0..100 // Example range, adjust as needed
        if (points.any { it !in allowedRange }) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Points out of allowed range")
        val winnerIdx = if (points[0] > points[1]) 0 else if (points[1] > points[0]) 1 else -1
        if (winnerIdx == -1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No winner (tie)")
        val winnerId = request.score[winnerIdx].playerId
        // Save scores
        request.score.forEach {
            scoreRepository.save(
                Score(
                    id = UUID.randomUUID(),
                    organizationId = orgId,
                    match = match,
                    playerId = it.playerId,
                    score = it.points,
                    submittedAt = Instant.now()
                )
            )
        }
        // Optimistic concurrency: update match status
        try {
            matchRepository.save(match.copy(
                status = "COMPLETED",
                completedTime = Instant.now(),
                winnerId = winnerId
            ))
        } catch (e: OptimisticLockingFailureException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Concurrent update detected")
        }
        // Update standings/ladder positions (skeleton)
        updateStandingsAfterMatch(match.id)
        // Publish domain event
        eventPublisher.publishEvent(MatchScoreSubmittedEvent(match.id, orgId, winnerId, request.score, request.notes))
    }

    fun updateStandingsAfterMatch(matchId: UUID) {
        // TODO: Implement standings/ladder update logic
    }

    fun importPlayers(orgId: UUID, leagueId: UUID, file: MultipartFile): ImportSummary {
        val reader = BufferedReader(InputStreamReader(file.inputStream))
        val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        val imported = mutableListOf<ImportPlayerResult>()
        val emails = mutableSetOf<String>()
        reader.lineSequence().drop(1).forEachIndexed { idx, line ->
            val cols = line.split(",")
            if (cols.size < 2) {
                imported.add(ImportPlayerResult(idx + 2, line, "Invalid column count"))
                return@forEachIndexed
            }
            val name = cols[0].trim()
            val email = cols[1].trim().lowercase()
            val dupr = cols.getOrNull(2)?.trim()
            val partnerEmail = cols.getOrNull(3)?.trim()?.lowercase()
            if (!emailPattern.matcher(email).matches()) {
                imported.add(ImportPlayerResult(idx + 2, line, "Invalid email"))
                return@forEachIndexed
            }
            if (emails.contains(email)) {
                imported.add(ImportPlayerResult(idx + 2, line, "Duplicate email"))
                return@forEachIndexed
            }
            emails.add(email)
            // TODO: Create or link user, add to league, handle partner
            imported.add(ImportPlayerResult(idx + 2, line, "Imported"))
        }
        return ImportSummary(imported)
    }

    fun exportResults(orgId: UUID, leagueId: UUID, format: String): String {
        // TODO: Query results, format as CSV or DUPR spec
        return "player_name,email,dupr_rating,match_results\n..."
    }

    fun getActiveLeaguesForMobile(): List<MobileLeagueSummary> {
        // TODO: Query active leagues, compute next-action hints
        return listOf(
            MobileLeagueSummary(UUID.randomUUID(), "League A", "active", "checkin"),
            MobileLeagueSummary(UUID.randomUUID(), "League B", "active", "submit-score")
        )
    }

    fun mobileCheckinMatch(matchId: UUID, request: MobileCheckinRequest): MobileActionResult {
        // TODO: Validate, record check-in, compute next action
        return MobileActionResult(true, "Checked in", "submit-score")
    }

    fun mobileSubmitScore(matchId: UUID, request: MobileSubmitScoreRequest): MobileActionResult {
        // TODO: Validate, record score, compute next action
        return MobileActionResult(true, "Score submitted", "view-standings")
    }
}

// Domain event
class MatchScoreSubmittedEvent(
    val matchId: UUID,
    val orgId: UUID,
    val winnerId: UUID,
    val score: List<ScoreEntry>,
    val notes: String?
)

data class ImportPlayerResult(val row: Int, val raw: String, val status: String)
data class ImportSummary(val results: List<ImportPlayerResult>)
data class MobileActionResult(val success: Boolean, val message: String, val nextAction: String?)
