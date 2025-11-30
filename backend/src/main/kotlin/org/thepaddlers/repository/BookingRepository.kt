package org.thepaddlers.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.thepaddlers.model.Booking
import java.time.LocalDateTime
import jakarta.persistence.LockModeType

interface BookingRepository : JpaRepository<Booking, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.resourceId = :resourceId AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    fun findOverlappingBookings(
        @Param("resourceId") resourceId: Long,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime
    ): List<Booking>
}
