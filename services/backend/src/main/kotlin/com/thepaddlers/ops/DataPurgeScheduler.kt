package com.thepaddlers.ops

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Instant

@Component
class DataPurgeScheduler(
    private val jdbcTemplate: JdbcTemplate,
    @Value("\${data.retention.days:365}") private val retentionDays: Long
) {
    @Scheduled(cron = "0 0 3 * * *") // daily at 3am
    fun purgeOldDeleted() {
        val cutoff = Instant.now().minusSeconds(retentionDays * 24 * 60 * 60)
        jdbcTemplate.update("DELETE FROM users WHERE deleted_at IS NOT NULL AND deleted_at < ?", cutoff)
        jdbcTemplate.update("DELETE FROM reservations WHERE deleted_at IS NOT NULL AND deleted_at < ?", cutoff)
        jdbcTemplate.update("DELETE FROM leagues WHERE deleted_at IS NOT NULL AND deleted_at < ?", cutoff)
        jdbcTemplate.update("DELETE FROM tournaments WHERE deleted_at IS NOT NULL AND deleted_at < ?", cutoff)
    }
}

