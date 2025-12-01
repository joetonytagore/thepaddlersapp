package com.thepaddlers.reservations

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@Testcontainers
class ConcurrentReservationTest @Autowired constructor(
    private val reservationService: ReservationService
) {
    @Test
    fun `concurrent createReservation only allows one`() {
        val courtId = UUID.randomUUID()
        val userIds = List(10) { UUID.randomUUID() }
        val start = Instant.now().plusSeconds(3600)
        val end = start.plusSeconds(3600)
        val latch = CountDownLatch(userIds.size)
        val executor = Executors.newFixedThreadPool(userIds.size)
        val results = Collections.synchronizedList(mutableListOf<Boolean>())
        userIds.forEach { userId ->
            executor.submit {
                try {
                    reservationService.createReservationWithRetry(courtId, userId, start, end)
                    results.add(true)
                } catch (e: Exception) {
                    results.add(false)
                } finally {
                    latch.countDown()
                }
            }
        }
        latch.await()
        assertEquals(1, results.count { it })
    }
}

