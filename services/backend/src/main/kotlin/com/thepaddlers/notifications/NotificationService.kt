package com.thepaddlers.notifications

import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class NotificationService(
    private val deviceRepository: DeviceRepository,
    private val notificationQueue: NotificationQueue // Redis/RabbitMQ wrapper
) {
    fun registerDevice(req: RegisterDeviceRequest) {
        val entity = DeviceEntity(
            deviceId = req.deviceId,
            userId = req.userId,
            platform = req.platform,
            pushToken = req.pushToken,
            lastSeen = Instant.now(),
            createdAt = Instant.now()
        )
        deviceRepository.save(entity)
    }
    fun unregisterDevice(deviceId: String) {
        deviceRepository.deleteById(deviceId)
    }
    fun sendPush(userId: UUID, title: String, body: String, data: Map<String, String>) {
        val devices = deviceRepository.findByUserId(userId)
        for (device in devices) {
            // TODO: Call FCM/APNs wrapper
            // If push fails, fallback to SMS/email
        }
    }
    fun scheduleReminder(matchId: UUID, userId: UUID, sendAt: Instant) {
        notificationQueue.enqueueScheduledNotification(matchId, userId, sendAt)
    }
    fun sendTestPush(userId: UUID, title: String, body: String) {
        sendPush(userId, title, body, emptyMap())
    }
}

