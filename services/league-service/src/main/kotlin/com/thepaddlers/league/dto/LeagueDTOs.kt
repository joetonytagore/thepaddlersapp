package com.thepaddlers.league.dto

import java.util.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

// League creation request
 data class CreateLeagueRequest(
    @field:NotBlank
    val name: String,
    val description: String? = null,
    @field:NotBlank
    val format: String,
    @field:NotNull
    val startTime: String?, // ISO8601
    val endTime: String?
)

// League response
 data class LeagueResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val format: String,
    val startTime: String?,
    val endTime: String?
)

// Player join request
 data class EnterLeagueRequest(
    @field:NotNull
    val playerId: UUID
)

// Score details
 data class ScoreEntry(
    @field:NotNull
    val playerId: UUID,
    @field:NotNull
    val points: Int
)

// Score submission request
 data class SubmitScoreRequest(
    @field:NotNull
    val submittedByUserId: UUID,
    @field:NotNull
    val score: List<ScoreEntry>,
    val notes: String? = null
)
