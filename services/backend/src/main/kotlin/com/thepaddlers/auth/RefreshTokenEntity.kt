package com.thepaddlers.auth
)
    val createdAt: Instant = Instant.now()
    @Column(nullable = false)
    val revoked: Boolean = false,
    @Column(nullable = false)
    val expiresAt: Instant,
    @Column(nullable = false)
    val userId: UUID,
    @Column(nullable = false)
    val token: String,
    @Column(nullable = false, unique = true)
    val id: UUID = UUID.randomUUID(),
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
data class RefreshTokenEntity(
@Table(name = "refresh_tokens")
@Entity

import java.util.*
import java.time.Instant
import jakarta.persistence.*


