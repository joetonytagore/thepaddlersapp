package com.thepaddlers.league.controllers

import com.thepaddlers.league.security.JwtUtil
import com.thepaddlers.league.security.Role
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController @Autowired constructor(private val jwtUtil: JwtUtil) {
    data class LoginRequest(
        val username: String,
        val password: String
    ) {
        companion object {
            fun example() = LoginRequest("player1", "password123")
        }
    }
    data class RegisterRequest(
        val username: String,
        val password: String
    ) {
        companion object {
            fun example() = RegisterRequest("newuser", "password456")
        }
    }
    data class AuthResponse(val token: String) {
        companion object {
            fun example() = AuthResponse("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        }
    }

    @Operation(
        summary = "Login and receive JWT",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = [Content(schema = Schema(implementation = LoginRequest::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "JWT token",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            )
        ]
    )
    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        val token = jwtUtil.generateToken(req.username, listOf(Role.PLAYER.name))
        return ResponseEntity.ok(AuthResponse(token))
    }

    @Operation(
        summary = "Register new user (defaults to PLAYER)",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = [Content(schema = Schema(implementation = RegisterRequest::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "JWT token",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            )
        ]
    )
    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<AuthResponse> {
        val token = jwtUtil.generateToken(req.username, listOf(Role.PLAYER.name))
        return ResponseEntity.ok(AuthResponse(token))
    }
}
