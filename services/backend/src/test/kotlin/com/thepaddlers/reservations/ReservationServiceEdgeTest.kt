package com.thepaddlers.reservations

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.*

class ReservationServiceEdgeTest {
    private val repo: ReservationRepository = mock()
    private val jdbc: org.springframework.jdbc.core.JdbcTemplate = mock()
    private val service = ReservationService(jdbc, repo)

    @Test
    fun `overlapping reservation throws`() = runBlocking {
        val courtId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val start = Instant.now()
        val end = start.plusSeconds(3600)
        whenever(jdbc.queryForObject(any(), eq(Int::class.java), any(), any(), any())).thenReturn(1)
        assertThrows(org.springframework.dao.ConcurrencyFailureException::class.java) {
            runBlocking { service.createReservation(courtId, userId, start, end) }
        }
    }

    @Test
    fun `non-overlapping reservation succeeds`() = runBlocking {
        val courtId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val start = Instant.now()
        val end = start.plusSeconds(3600)
        whenever(jdbc.queryForObject(any(), eq(Int::class.java), any(), any(), any())).thenReturn(0)
        whenever(repo.save(any())).thenReturn(mock())
        val res = runBlocking { service.createReservation(courtId, userId, start, end) }
        assertNotNull(res)
    }
}

