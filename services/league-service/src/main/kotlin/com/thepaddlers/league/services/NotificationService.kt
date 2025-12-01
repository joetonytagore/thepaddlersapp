package com.thepaddlers.league.services

import com.thepaddlers.league.repositories.NotificationLogRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class NotificationService @Autowired constructor(
    private val notificationLogRepository: NotificationLogRepository
) {
    @Transactional
    fun sendMatchReminder(userId: UUID, matchId: UUID, sendAt: Instant) {
        // TODO: Integrate with SendGrid/Twilio/APNs/FCM
        notificationLogRepository.save(
            NotificationLog(
                id = UUID.randomUUID(),
                orgId = UUID.randomUUID(), // TODO: pass orgId
                userId = userId,
                type = "MATCH_REMINDER",
                targetId = matchId,
                sentAt = sendAt,
                status = "SCHEDULED",
                message = "Match reminder scheduled"
            )
        )
    }

    @Transactional
    fun sendWaitlistOffer(userId: UUID, waitlistEntryId: UUID) {
        notificationLogRepository.save(
            NotificationLog(
                id = UUID.randomUUID(),
                orgId = UUID.randomUUID(), // TODO: pass orgId
                userId = userId,
                type = "WAITLIST_OFFER",
                targetId = waitlistEntryId,
                sentAt = Instant.now(),
                status = "SENT",
                message = "Waitlist offer sent"
            )
        )
    }

    @Transactional
    fun sendScoreSubmissionNotification(matchId: UUID, submittingUserId: UUID) {
        notificationLogRepository.save(
            NotificationLog(
                id = UUID.randomUUID(),
                orgId = UUID.randomUUID(), // TODO: pass orgId
                userId = submittingUserId,
                type = "SCORE_SUBMISSION",
                targetId = matchId,
                sentAt = Instant.now(),
                status = "SENT",
                message = "Score submitted"
            )
        )
    }

    fun listNotificationLogs(orgId: UUID): List<NotificationLog> {
        return notificationLogRepository.findByOrgId(orgId)
    }

    fun registerMobilePushToken(request: MobilePushRegisterRequest) {
        // TODO: Store device push token for user/device
    }
}

data class NotificationLog(
    val id: UUID,
    val orgId: UUID,
    val userId: UUID?,
    val type: String,
    val targetId: UUID?,
    val sentAt: Instant,
    val status: String,
    val message: String?
)

data class MobilePushRegisterRequest(
    val userId: UUID,
    val deviceType: String,
    val pushToken: String
)
