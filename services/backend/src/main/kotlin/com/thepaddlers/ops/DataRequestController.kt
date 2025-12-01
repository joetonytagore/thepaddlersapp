package com.thepaddlers.ops

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/orgs/{orgId}/data-request")
class DataRequestController {
    @PostMapping
    fun requestDataExportOrDeletion(
        @PathVariable orgId: UUID,
        @RequestBody request: DataRequest
    ): ResponseEntity<Void> {
        // TODO: Prepare export and/or schedule deletion for user
        // TODO: Send notification to admin/user
        return ResponseEntity.accepted().build()
    }
}

data class DataRequest(val userId: UUID, val export: Boolean = true, val delete: Boolean = false)

