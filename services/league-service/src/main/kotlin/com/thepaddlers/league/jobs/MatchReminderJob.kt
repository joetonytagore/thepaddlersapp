package com.thepaddlers.league.jobs

import com.thepaddlers.league.services.NotificationService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class MatchReminderJob(
    private val notificationService: NotificationService
) {
    // This is a skeleton. In production, query matches and schedule reminders 24h/1h before start.
    @Scheduled(fixedRate = 60000)
    fun scheduleReminders() {
        // TODO: Query upcoming matches, schedule reminders for users
        // notificationService.sendMatchReminder(userId, matchId, sendAt)
    }
}

