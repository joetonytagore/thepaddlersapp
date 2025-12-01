package com.thepaddlers.notifications

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class NotificationScheduler(
    private val notificationService: NotificationService,
    private val notificationQueue: NotificationQueue // Redis/RabbitMQ wrapper
) {
    @Scheduled(fixedRate = 60000)
    fun processScheduledNotifications() {
        val now = Instant.now()
        val notifications = notificationQueue.dequeueDueNotifications(now)
        for (n in notifications) {
            var attempts = 0
            var success = false
            var delay = 1000L
            while (!success && attempts < 5) {
                try {
                    notificationService.sendPush(n.userId, n.title, n.body, n.data)
                    success = true
                } catch (e: Exception) {
                    Thread.sleep(delay)
                    delay *= 2 // Exponential backoff
                    attempts++
                }
            }
            if (!success) {
                // TODO: Fallback to SMS/email
            }
        }
    }
}

