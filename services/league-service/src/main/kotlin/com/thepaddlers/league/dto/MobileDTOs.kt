package com.thepaddlers.league.dto

import java.util.*

// Compact league summary for mobile home screen
 data class MobileLeagueSummary(
    val id: UUID,
    val n: String, // name
    val s: String, // status
    val na: String?, // next action hint
)

// Mobile check-in request
 data class MobileCheckinRequest(
    val userId: UUID,
    val deviceId: String
)

// Mobile score submission request
 data class MobileSubmitScoreRequest(
    val userId: UUID,
    val score: List<ScoreEntry>
)

// Mobile push token registration
 data class MobilePushRegisterRequest(
    val userId: UUID,
    val deviceId: String,
    val token: String
)

// Compact action result
 data class MobileActionResult(
    val ok: Boolean,
    val msg: String?,
    val na: String? // next action hint
)

