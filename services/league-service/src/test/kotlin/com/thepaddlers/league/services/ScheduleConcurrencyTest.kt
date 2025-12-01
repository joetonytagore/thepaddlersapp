package com.thepaddlers.league.services

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@Testcontainers
class ScheduleConcurrencyTest @Autowired constructor(
    private val leagueService: LeagueService
) {
    private val leagueId = UUID.randomUUID()

    @RepeatedTest(3)
    fun `concurrent schedule generation only creates one schedule`() {
        val executor = Executors.newFixedThreadPool(2)
        val latch = CountDownLatch(2)
        val results = Collections.synchronizedList(mutableListOf<Boolean>())
        repeat(2) {
            executor.submit {
                try {
                    leagueService.generateSchedule(leagueId, "round-robin")
                    results.add(true)
                } catch (e: Exception) {
                    results.add(false)
                } finally {
                    latch.countDown()
                }
            }
        }
        latch.await(10, TimeUnit.SECONDS)
        // Only one schedule should be created (idempotency)
        val schedules = leagueService.getSchedulesForLeague(leagueId)
        assertEquals(1, schedules.size)
    }
}

