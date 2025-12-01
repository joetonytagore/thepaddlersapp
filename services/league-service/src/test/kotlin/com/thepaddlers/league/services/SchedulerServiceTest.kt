package com.thepaddlers.league.services

import com.thepaddlers.league.entities.League
import com.thepaddlers.league.entities.Match
import com.thepaddlers.league.repositories.MatchRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.*
import java.util.*

class SchedulerServiceTest {
    private val matchRepository: MatchRepository = mock()
    private val schedulerService = SchedulerService(matchRepository)
    private val orgId = UUID.randomUUID()
    private val league = League(id = UUID.randomUUID(), organizationId = orgId, name = "Test League", status = "ACTIVE")

    @Test
    fun `returns empty list if less than 2 players`() {
        val result = schedulerService.generateRoundRobinSchedule(league, listOf(UUID.randomUUID()), 2, LocalDate.now(), 60, listOf(DayOfWeek.MONDAY), LocalTime.of(9,0), LocalTime.of(12,0))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `adds bye for odd number of players`() {
        val players = List(5) { UUID.randomUUID() }
        val result = schedulerService.generateRoundRobinSchedule(league, players, 2, LocalDate.now(), 60, listOf(DayOfWeek.MONDAY), LocalTime.of(9,0), LocalTime.of(12,0))
        // Should schedule with 6 players (5 + 1 bye)
        val expectedMatches = 6 * 5 / 2 // n*(n-1)/2 for round robin
        assertEquals(expectedMatches, result.size)
    }

    @Test
    fun `no overlapping matches for same court`() {
        val players = List(6) { UUID.randomUUID() }
        val result = schedulerService.generateRoundRobinSchedule(league, players, 2, LocalDate.now(), 60, listOf(DayOfWeek.MONDAY), LocalTime.of(9,0), LocalTime.of(12,0))
        val matchesByCourtAndTime = result.groupBy { it.scheduledTime }
        matchesByCourtAndTime.values.forEach { matchesAtTime ->
            assertTrue(matchesAtTime.size <= 2) // 2 courts
        }
    }

    @Test
    fun `respects time window boundaries`() {
        val players = List(4) { UUID.randomUUID() }
        val result = schedulerService.generateRoundRobinSchedule(league, players, 1, LocalDate.now(), 60, listOf(DayOfWeek.MONDAY), LocalTime.of(10,0), LocalTime.of(12,0))
        result.forEach { match ->
            val localTime = match.scheduledTime.atZone(ZoneId.systemDefault()).toLocalTime()
            assertTrue(localTime >= LocalTime.of(10,0) && localTime <= LocalTime.of(12,0))
        }
    }

    @Test
    fun `handles large player count`() {
        val players = List(20) { UUID.randomUUID() }
        val result = schedulerService.generateRoundRobinSchedule(league, players, 4, LocalDate.now(), 30, listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), LocalTime.of(8,0), LocalTime.of(18,0))
        val expectedMatches = 20 * 19 / 2
        assertEquals(expectedMatches, result.size)
    }

    @Test
    fun `throws on invalid input`() {
        val players = List(4) { UUID.randomUUID() }
        assertThrows(IllegalArgumentException::class.java) {
            schedulerService.generateRoundRobinSchedule(league, players, 0, LocalDate.now(), 60, listOf(DayOfWeek.MONDAY), LocalTime.of(9,0), LocalTime.of(12,0))
        }
        assertThrows(IllegalArgumentException::class.java) {
            schedulerService.generateRoundRobinSchedule(league, players, 2, LocalDate.now(), -10, listOf(DayOfWeek.MONDAY), LocalTime.of(9,0), LocalTime.of(12,0))
        }
    }
}

