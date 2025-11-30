package org.thepaddlers.controller

import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.thepaddlers.model.Booking
import org.thepaddlers.repository.BookingRepository
import java.time.LocalDateTime
import org.springframework.security.access.prepost.PreAuthorize
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/bookings")
class BookingController(
    private val bookingRepo: BookingRepository
) {
    private val logger = LoggerFactory.getLogger(BookingController::class.java)

    data class BookingRequest(
        val resourceId: Long,
        val userId: Long,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime
    )

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Transactional
    fun createBooking(@RequestBody req: BookingRequest): ResponseEntity<Any> {
        logger.info("Admin ${req.userId} created booking for resource ${req.resourceId} from ${req.startTime} to ${req.endTime}")
        val overlaps = bookingRepo.findOverlappingBookings(req.resourceId, req.startTime, req.endTime)
        if (overlaps.isNotEmpty()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Double booking detected"))
        }
        val booking = Booking(
            resourceId = req.resourceId,
            userId = req.userId,
            startTime = req.startTime,
            endTime = req.endTime
        )
        bookingRepo.save(booking)
        return ResponseEntity.ok(mapOf("id" to booking.id))
    }
}
