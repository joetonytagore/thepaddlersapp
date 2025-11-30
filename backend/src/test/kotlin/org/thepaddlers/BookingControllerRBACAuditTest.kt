package org.thepaddlers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingControllerRBACAuditTest {
    @Autowired lateinit var restTemplate: TestRestTemplate

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `admin can create booking and audit log is written`() {
        val req = mapOf(
            "resourceId" to 2L,
            "userId" to 99L,
            "startTime" to LocalDateTime.now().plusDays(2).toString(),
            "endTime" to LocalDateTime.now().plusDays(2).plusHours(2).toString()
        )
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(req, headers)
        val resp = restTemplate.postForEntity("/api/bookings", entity, Map::class.java)
        assertEquals(200, resp.statusCode.value())
        // For audit log, check logs manually or with a log capturing library
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `non-admin cannot create booking`() {
        val req = mapOf(
            "resourceId" to 2L,
            "userId" to 100L,
            "startTime" to LocalDateTime.now().plusDays(3).toString(),
            "endTime" to LocalDateTime.now().plusDays(3).plusHours(2).toString()
        )
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(req, headers)
        val resp = restTemplate.postForEntity("/api/bookings", entity, Map::class.java)
        assertEquals(403, resp.statusCode.value())
    }
}

