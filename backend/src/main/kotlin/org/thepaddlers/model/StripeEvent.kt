package org.thepaddlers.model

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class StripeEvent(
    @Id val id: String,
    val type: String
)
