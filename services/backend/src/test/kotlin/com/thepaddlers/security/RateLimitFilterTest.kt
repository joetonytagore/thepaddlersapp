package com.thepaddlers.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.util.*

class RateLimitFilterTest {
    private val redisTemplate: RedisTemplate<String, String> = mock()
    private val config = RateLimitConfig().apply {
        loginAttempts = 2
        loginWindowMinutes = 1
        reservationPerMinute = 2
    }
    private val filter = RateLimitFilter(redisTemplate, config)

    @Test
    fun `login attempts per IP and user are rate limited`() {
        val req = MockHttpServletRequest("POST", "/auth/login")
        req.addParameter("username", "testuser")
        val res = MockHttpServletResponse()
        whenever(redisTemplate.opsForValue().increment("rl:login:ip:127.0.0.1")).thenReturn(3)
        whenever(redisTemplate.opsForValue().increment("rl:login:user:testuser")).thenReturn(3)
        filter.doFilter(req, res) { }
        assertEquals(429, res.status)
    }

    @Test
    fun `reservation requests per user are rate limited`() {
        val req = MockHttpServletRequest("POST", "/reservations")
        req.addParameter("userId", "user-1")
        val res = MockHttpServletResponse()
        whenever(redisTemplate.opsForValue().increment("rl:reservation:user-1")).thenReturn(3)
        filter.doFilter(req, res) { }
        assertEquals(429, res.status)
    }
}

