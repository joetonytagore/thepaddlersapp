package org.thepaddlers.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Column
import java.time.LocalDateTime

@Entity
data class Booking(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    val resourceId: Long,
    @Column(nullable = false)
    val userId: Long,
    @Column(nullable = false)
    val startTime: LocalDateTime,
    @Column(nullable = false)
    val endTime: LocalDateTime
)

