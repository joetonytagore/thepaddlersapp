package org.thepaddlers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"])
class BookingConcurrencyTest {
    @Autowired lateinit var restTemplate: TestRestTemplate

    @Test
    fun `concurrent booking requests do not double-book`() {
        val resourceId = 1L
        val userId = 1L
        val startTime = LocalDateTime.now().plusDays(1)
        val endTime = startTime.plusHours(2)
        val req = mapOf(
            "resourceId" to resourceId,
            "userId" to userId,
            "startTime" to startTime.toString(),
            "endTime" to endTime.toString()
        )
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(req, headers)
        val latch = CountDownLatch(2)
        val results = mutableListOf<Int>()
        val executor = Executors.newFixedThreadPool(2)
        repeat(2) {
            executor.submit {
                val resp = restTemplate.postForEntity("/api/bookings", entity, Map::class.java)
                synchronized(results) {
                    results.add(resp.statusCode.value())
                }
                latch.countDown()
            }
        }
        latch.await()
        executor.shutdown()
        // Only one booking should succeed, the other should fail with 400
        assertEquals(listOf(200, 400).sorted(), results.sorted())
    }
}

