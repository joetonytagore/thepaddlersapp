package com.thepaddlers.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import java.time.Duration

@Component
@Order(1)
class RateLimitFilter @Autowired constructor(
    private val redisTemplate: RedisTemplate<String, String>?,
    private val config: RateLimitConfig
) : GenericFilterBean() {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (redisTemplate == null) {
            chain.doFilter(request, response)
            return
        }
        val req = request as HttpServletRequest
        val res = response as HttpServletResponse
        val ip = req.remoteAddr ?: "unknown"
        val path = req.requestURI
        val method = req.method
        if (path == "/auth/login" && method == "POST") {
            val username = req.getParameter("username") ?: req.getParameter("email") ?: "unknown"
            val ipKey = "rl:login:ip:$ip"
            val userKey = "rl:login:user:$username"
            val ipCount = redisTemplate.opsForValue().increment(ipKey)
            val userCount = redisTemplate.opsForValue().increment(userKey)
            redisTemplate.expire(ipKey, Duration.ofMinutes(config.loginWindowMinutes.toLong()))
            redisTemplate.expire(userKey, Duration.ofMinutes(config.loginWindowMinutes.toLong()))
            if (ipCount != null && ipCount > config.loginAttempts) {
                res.status = 429
                res.setHeader("Retry-After", (config.loginWindowMinutes * 60).toString())
                res.writer.write("Too many login attempts from IP")
                return
            }
            if (userCount != null && userCount > config.loginAttempts) {
                res.status = 429
                res.setHeader("Retry-After", (config.loginWindowMinutes * 60).toString())
                res.writer.write("Too many login attempts for user")
                return
            }
        }
        if (path.startsWith("/reservations") && method == "POST") {
            val userId = req.getParameter("userId") ?: req.getHeader("X-User-Id") ?: "unknown"
            val key = "rl:reservation:$userId"
            val count = redisTemplate.opsForValue().increment(key)
            redisTemplate.expire(key, Duration.ofMinutes(1))
            if (count != null && count > config.reservationPerMinute) {
                res.status = 429
                res.setHeader("Retry-After", "60")
                res.writer.write("Too many reservation requests")
                return
            }
        }
        chain.doFilter(request, response)
    }
}

