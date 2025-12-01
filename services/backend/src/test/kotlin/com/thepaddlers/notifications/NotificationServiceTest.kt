package com.thepaddlers.notifications

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.*

class NotificationServiceTest {
    private val deviceRepository: DeviceRepository = mock()
    private val notificationQueue: NotificationQueue = mock()
    private val service = NotificationService(deviceRepository, notificationQueue)
    private val userId = UUID.randomUUID()

    @Test
    fun `register and unregister device`() {
        val req = RegisterDeviceRequest("dev1", userId, "FCM", "token123")
        service.registerDevice(req)
        service.unregisterDevice("dev1")
        // No exceptions means success
    }

    @Test
    fun `schedule and send push notification`() {
        whenever(deviceRepository.findByUserId(userId)).thenReturn(listOf(DeviceEntity("dev1", userId, "FCM", "token123", Instant.now(), Instant.now())))
        service.sendPush(userId, "Test", "Body", mapOf("key" to "value"))
        service.scheduleReminder(UUID.randomUUID(), userId, Instant.now().plusSeconds(60))
        // No exceptions means success
    }
}

