package com.thepaddlers.league.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "leagues")
data class League(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val organizationId: UUID,
    @Column(nullable = false)
    val name: String,
    val description: String? = null,
    @Column(nullable = false)
    val format: String,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    @Column(nullable = false)
    val status: String = "ACTIVE"
)
