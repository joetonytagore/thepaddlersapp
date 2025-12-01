package com.thepaddlers.audit

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/admin/audit")
class AdminAuditController(
    private val auditRepository: AuditRepository
) {
    @GetMapping
    fun getAuditEntries(
        @RequestParam orgId: UUID,
        @RequestParam(required = false) action: String?,
        @RequestParam(required = false) from: Instant?,
        @RequestParam(required = false) to: Instant?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<List<AuditEntry>> {
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val entries = auditRepository.findAllByOrgIdAndActionAndCreatedAtBetween(
            orgId, action, from ?: Instant.EPOCH, to ?: Instant.now(), pageable
        )
        return ResponseEntity.ok(entries.content)
    }
}

