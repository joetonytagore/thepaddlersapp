package com.thepaddlers.league.dto

import java.util.*
import jakarta.validation.constraints.NotNull
import com.thepaddlers.league.entities.QueueableType

// Join waitlist request
 data class JoinWaitlistRequest(
    @field:NotNull val queueableType: QueueableType,
    @field:NotNull val queueableId: UUID,
    @field:NotNull val userId: UUID
)

// Accept offer request
 data class AcceptOfferRequest(
    @field:NotNull val userId: UUID
)

