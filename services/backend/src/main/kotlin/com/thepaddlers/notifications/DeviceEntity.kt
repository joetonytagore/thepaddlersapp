package com.thepaddlers.notifications
)
    @Column(nullable = false) val createdAt: Instant = Instant.now()
    @Column(nullable = false) val lastSeen: Instant = Instant.now(),
    @Column(nullable = false) val pushToken: String,
    @Column(nullable = false) val platform: String, // FCM, APNs
    @Column(nullable = false) val userId: UUID,
    @Id @Column(name = "device_id") val deviceId: String,
data class DeviceEntity(
@Table(name = "devices")
@Entity

import java.util.*
import java.time.Instant
import jakarta.persistence.*


