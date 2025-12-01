package com.thepaddlers.league.services

import com.thepaddlers.league.entities.Match
import com.thepaddlers.league.entities.League
import com.thepaddlers.league.repositories.MatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.beans.factory.annotation.Autowired
import java.time.*
import java.util.*

@Service
class SchedulerService(
    private val matchRepository: MatchRepository,
    @Autowired private val jdbcTemplate: JdbcTemplate
) {
    @Transactional
    suspend fun generateRoundRobinSchedule(
        league: League,
        players: List<UUID>,
        courts: Int,
        startDate: LocalDate,
        matchDurationMinutes: Int,
        daysOfWeek: List<DayOfWeek>,
        timeWindowStart: LocalTime,
        timeWindowEnd: LocalTime
    ): List<Match> = withContext(Dispatchers.Default) {
        val lockKey = league.id.mostSignificantBits xor league.id.leastSignificantBits
        val gotLock = withContext(Dispatchers.IO) {
            jdbcTemplate.queryForObject("SELECT pg_try_advisory_xact_lock(?)", Boolean::class.java, lockKey)
        }
        if (gotLock != true) throw IllegalStateException("Could not acquire advisory lock for schedule generation")
        if (players.size < 2) return@withContext emptyList()
        val playerList = if (players.size % 2 == 0) players.toMutableList() else (players + UUID.randomUUID()).toMutableList() // Add bye if odd
        val rounds = playerList.size - 1
        val matchesPerRound = playerList.size / 2
        val schedule = mutableListOf<Match>()
        val matchSlotsPerDay = ((Duration.between(timeWindowStart, timeWindowEnd).toMinutes().toLong() / matchDurationMinutes.toLong()) * courts.toLong()).toInt()
        var currentDate = startDate
        var slotIndex = 0
        var dayIndex = 0
        // Circle method
        for (round in 0 until rounds) {
            val pairs = mutableListOf<Pair<UUID, UUID>>()
            for (i in 0 until matchesPerRound) {
                val p1 = playerList[i]
                val p2 = playerList[playerList.size - 1 - i]
                if (p1 != p2) pairs.add(Pair(p1, p2))
            }
            // Rotate players except first
            playerList.drop(1).let {
                val rotated = listOf(playerList.first()) + it.dropLast(1).reversed() + listOf(it.last())
                for (i in 1 until playerList.size) playerList[i] = rotated[i]
            }
            for (pair in pairs) {
                // Find next available slot
                val dayOfWeek = daysOfWeek[dayIndex % daysOfWeek.size]
                while (currentDate.dayOfWeek != dayOfWeek) currentDate = currentDate.plusDays(1)
                val slotTime = timeWindowStart.plusMinutes((((slotIndex % matchSlotsPerDay) / courts) * matchDurationMinutes).toLong())
                val scheduledTime = currentDate.atTime(slotTime)
                val match = Match(
                    id = UUID.randomUUID(),
                    organizationId = league.organizationId,
                    league = league,
                    group = null,
                    player1Id = pair.first,
                    player2Id = pair.second,
                    scheduledTime = scheduledTime.atZone(ZoneId.systemDefault()).toInstant(),
                    completedTime = null,
                    status = "scheduled",
                    winnerId = null,
                    createdAt = Instant.now()
                )
                schedule.add(match)
                slotIndex++
                if (slotIndex % matchSlotsPerDay == 0) {
                    currentDate = currentDate.plusDays(1)
                    dayIndex++
                }
            }
        }
        // Non-blocking DB call
        withContext(Dispatchers.IO) {
            matchRepository.saveAll(schedule)
        }
        schedule
    }
}
