package com.thepaddlers.reservations

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.dao.ConcurrencyFailureException
import java.time.Instant
import java.util.*

class ReservationServiceTest {
    private val jdbc: org.springframework.jdbc.core.JdbcTemplate = mock()
    private val repo: ReservationRepository = mock()
    private val service = ReservationService(jdbc, repo)
    private val courtId = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private val baseStart = Instant.parse("2025-12-01T10:00:00Z")
    private val baseEnd = Instant.parse("2025-12-01T11:00:00Z")

    private fun stubOverlap(count: Int) {
        whenever(jdbc.queryForMap(any(), any())).thenReturn(mapOf("court_id" to courtId))
        whenever(jdbc.queryForObject(any(), eq(Int::class.java), any(), any(), any())).thenReturn(count)
        whenever(repo.save(any())).thenAnswer { it.arguments[0] }
    }

    @Test
    fun `exact overlap throws`() = runBlocking {
        stubOverlap(1)
        assertThrows(ConcurrencyFailureException::class.java) {
            runBlocking { service.createReservation(courtId, userId, baseStart, baseEnd) }
        }
    }

    @Test
    fun `partial overlap throws`() = runBlocking {
        stubOverlap(1)
        val start = baseStart.plusSeconds(1800) // 10:30
        val end = baseEnd.plusSeconds(1800)     // 11:30
        assertThrows(ConcurrencyFailureException::class.java) {
            runBlocking { service.createReservation(courtId, userId, start, end) }
        }
    }

    @Test
    fun `contained inside throws`() = runBlocking {
        stubOverlap(1)
        val start = baseStart.plusSeconds(900)  // 10:15
        val end = baseEnd.minusSeconds(900)     // 10:45
        assertThrows(ConcurrencyFailureException::class.java) {
            runBlocking { service.createReservation(courtId, userId, start, end) }
        }
    }

    @Test
    fun `surrounding overlap throws`() = runBlocking {
        stubOverlap(1)
        val start = baseStart.minusSeconds(1800) // 9:30
        val end = baseEnd.plusSeconds(1800)      // 11:30
        assertThrows(ConcurrencyFailureException::class.java) {
            runBlocking { service.createReservation(courtId, userId, start, end) }
        }
    }

    @Test
    fun `adjacent bookings allowed`() = runBlocking {
        stubOverlap(0)
        val start = baseEnd // 11:00
        val end = baseEnd.plusSeconds(3600) // 12:00
        val res = runBlocking { service.createReservation(courtId, userId, start, end) }
        assertNotNull(res)
        assertEquals(start, res.startTime)
        assertEquals(end, res.endTime)
    }
}

