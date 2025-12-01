package com.thepaddlers.league.security

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecurityTests(@Autowired val restTemplate: TestRestTemplate) {
    @Test
    fun `PLAYER cannot access user endpoint`() {
        val token = "Bearer stub-player-token"
        val headers = HttpHeaders().apply { set("Authorization", token) }
        val entity = HttpEntity<String>(null, headers)
        val response = restTemplate.exchange("/api/user", HttpMethod.GET, entity, String::class.java)
        assert(response.statusCode == HttpStatus.FORBIDDEN)
    }
    @Test
    fun `SUPER_ADMIN can access user endpoint`() {
        val token = "Bearer stub-superadmin-token"
        val headers = HttpHeaders().apply { set("Authorization", token) }
        val entity = HttpEntity<String>(null, headers)
        val response = restTemplate.exchange("/api/user", HttpMethod.GET, entity, String::class.java)
        assert(response.statusCode == HttpStatus.OK)
    }
}

