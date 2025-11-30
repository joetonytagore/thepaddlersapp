package org.thepaddlers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.containers.PostgreSQLContainer
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.time.OffsetDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConcurrentBookingIntegrationTest {
    companion object {
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        init {
            postgres.start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun registerPgProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
        }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `only one booking is created under high concurrency`() {
        // Setup: create court and user
        val courtId = 1L
        val userId = 1L
        val start = OffsetDateTime.now().plusDays(1).withNano(0)
        val end = start.plusHours(1)
        val payload = mapOf(
            "court" to mapOf("id" to courtId),
            "user" to mapOf("id" to userId),
            "startAt" to start.toString(),
            "endAt" to end.toString()
        )
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(payload, headers)

        val threads = 50
        val latch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(threads)
        val responses = mutableListOf<ResponseEntity<Map<*, *>>>()

        repeat(threads) {
            executor.submit {
                latch.await()
                val resp = restTemplate.postForEntity("/api/bookings", entity, Map::class.java)
                synchronized(responses) { responses.add(resp) }
            }
        }
        latch.countDown()
        executor.shutdown()
        while (!executor.isTerminated) { Thread.sleep(50) }

        // Query all bookings for the court/time
        val bookingsResp = restTemplate.getForEntity("/api/bookings?courtId=$courtId", List::class.java)
        val bookings = bookingsResp.body ?: emptyList<Any>()
        assertEquals(1, bookings.size, "Only one booking should exist for the same court/time slot")
    }
}

