package org.thepaddlers.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.thepaddlers.model.StripeEvent

interface StripeEventRepository : JpaRepository<StripeEvent, String>

