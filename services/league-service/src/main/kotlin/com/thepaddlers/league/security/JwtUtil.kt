package com.thepaddlers.league.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.Claims
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil {
    private val secret = "your_jwt_secret_key"
    private val expirationMs = 86400000 // 1 day

    fun generateToken(username: String, roles: List<String>): String {
        val claims = HashMap<String, Any>()
        claims["roles"] = roles
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact()
    }

    fun extractUsername(token: String): String = getClaims(token).subject
    fun extractRoles(token: String): List<String> = (getClaims(token)["roles"] as List<*>).map { it.toString() }
    fun validateToken(token: String, username: String): Boolean {
        val claims = getClaims(token)
        return claims.subject == username && !claims.expiration.before(Date())
    }
    private fun getClaims(token: String): Claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).body
}
