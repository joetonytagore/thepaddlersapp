package com.thepaddlers.auth

import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val refreshTokenService: RefreshTokenService,
    private val userService: UserService,
    private val jwtService: JwtService
) {
    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        val user = userService.findByEmail(req.email) ?: return ResponseEntity.status(401).build()
        if (!BCrypt.checkpw(req.password, user.passwordHash)) return ResponseEntity.status(401).build()
        if (!user.emailVerified) return ResponseEntity.status(403).body(AuthResponse(error = "Email not verified"))
        val accessToken = jwtService.generateToken(user.id)
        val refreshToken = refreshTokenService.create(user.id)
        return ResponseEntity.ok(AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken.token,
            expiresIn = jwtService.expiresInSeconds,
            error = null
        ))
    }
    @PostMapping("/refresh")
    fun refresh(@RequestBody req: RefreshRequest): ResponseEntity<AuthResponse> {
        val entity = refreshTokenService.validate(req.refreshToken) ?: return ResponseEntity.status(401).build()
        val (newToken, _) = refreshTokenService.rotate(req.refreshToken, entity.userId)
        val accessToken = jwtService.generateToken(entity.userId)
        return ResponseEntity.ok(AuthResponse(
            accessToken = accessToken,
            refreshToken = newToken.token,
            expiresIn = jwtService.expiresInSeconds,
            error = null
        ))
    }
    @PostMapping("/revoke-refresh")
    fun revokeRefresh(@RequestBody req: RevokeRequest): ResponseEntity<Void> {
        if (req.allDevices) {
            refreshTokenService.revokeAll(req.userId)
        } else {
            refreshTokenService.revoke(req.refreshToken)
        }
        return ResponseEntity.ok().build()
    }
    @PostMapping("/request-verification")
    fun requestVerification(@RequestBody req: VerificationRequest): ResponseEntity<Void> {
        val user = userService.findByEmail(req.email) ?: return ResponseEntity.ok().build()
        val token = jwtService.generateVerificationToken(user.id)
        // TODO: Send verification email via SendGrid
        return ResponseEntity.ok().build()
    }
    @GetMapping("/verify")
    fun verifyEmail(@RequestParam token: String): ResponseEntity<Void> {
        val userId = jwtService.verifyVerificationToken(token) ?: return ResponseEntity.status(400).build()
        userService.markEmailVerified(userId)
        return ResponseEntity.ok().build()
    }
}

data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val accessToken: String? = null, val refreshToken: String? = null, val expiresIn: Long? = null, val error: String? = null)
data class RefreshRequest(val refreshToken: String)
data class RevokeRequest(val refreshToken: String, val userId: UUID, val allDevices: Boolean = false)
data class VerificationRequest(val email: String)

