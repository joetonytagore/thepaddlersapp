import io.micrometer.core.instrument.Timer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.dao.ConcurrencyFailureException
import java.time.Instant
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReservationService(
    private val jdbcTemplate: JdbcTemplate,
    private val reservationRepository: ReservationRepository
) {
    suspend fun createReservation(courtId: UUID, userId: UUID, start: Instant, end: Instant): Reservation = withContext(Dispatchers.IO) {
        // Lock the court row
        val courtRow = jdbcTemplate.queryForMap("SELECT * FROM court_locks WHERE court_id = ? FOR UPDATE", courtId)
        // Check for overlapping reservations
        val overlapCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM reservations WHERE court_id = ? AND deleted_at IS NULL AND ((start_time, end_time) OVERLAPS (?, ?))",
            Int::class.java, courtId, start, end
        )
        if (overlapCount > 0) throw ConcurrencyFailureException("Court already reserved for this time")
        // Insert reservation
        val reservation = Reservation(
            id = UUID.randomUUID(),
            courtId = courtId,
            userId = userId,
            startTime = start,
            endTime = end,
            createdAt = Instant.now(),
            deletedAt = null
        )
        reservationRepository.save(reservation)
        reservation
    }
    suspend fun createReservationWithRetry(courtId: UUID, userId: UUID, start: Instant, end: Instant, maxRetries: Int = 3): Reservation = withContext(Dispatchers.IO) {
        var attempt = 0
        while (true) {
            try {
                return@withContext createReservation(courtId, userId, start, end)
            } catch (e: ConcurrencyFailureException) {
                if (++attempt >= maxRetries) throw e
                kotlinx.coroutines.delay(100L * attempt)
            }
        }
    }
}
