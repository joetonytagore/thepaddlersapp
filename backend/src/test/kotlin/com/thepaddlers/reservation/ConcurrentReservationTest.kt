package com.thepaddlers.reservation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ConcurrentReservationTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("thepaddlers_test")
            withUsername("test")
            withPassword("test")
        }
        const val RESERVATION_ENDPOINT = "/api/bookings"
    }

    @LocalServerPort
    var port: Int = 0

    private val rest = RestTemplate()

    @Test
    fun `only one booking should succeed when N concurrent requests hit same slot`() {
        val now = LocalDateTime.now().plusHours(1)
        val start = now.toString()
        val end = now.plusHours(2).toString()

        val bookingJson = """
            {
              "resourceId": 1,
              "userId": 2,
              "startTime": "$start",
              "endTime": "$end"
            }
        """.trimIndent()

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val payload = HttpEntity(bookingJson, headers)

        val threads = 30
        val executor = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)
        val successes = java.util.concurrent.atomic.AtomicInteger(0)
        val failures = java.util.concurrent.atomic.AtomicInteger(0)

        repeat(threads) {
            executor.submit {
                try {
                    val resp = rest.postForEntity("http://localhost:$port$RESERVATION_ENDPOINT", payload, String::class.java)
                    if (resp.statusCode.is2xxSuccessful) successes.incrementAndGet() else failures.incrementAndGet()
                } catch (ex: Exception) {
                    failures.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        val completed = latch.await(30, TimeUnit.SECONDS)
        executor.shutdown()

        assertEquals(1, successes.get(), "Exactly one request should have succeeded; concurrent protection failed")
    }
}
