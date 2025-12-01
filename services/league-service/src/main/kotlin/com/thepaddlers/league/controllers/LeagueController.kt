package com.thepaddlers.league.controllers

import com.thepaddlers.league.dto.*
import com.thepaddlers.league.services.LeagueService
import com.thepaddlers.league.services.NotificationService
import com.thepaddlers.league.services.StandingsService
import com.thepaddlers.league.services.WaitlistService
import com.thepaddlers.league.entities.NotificationLog
import com.thepaddlers.league.services.StandingRow
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@Tag(name = "League", description = "League management endpoints")
@RequestMapping("/orgs/{orgId}")
class LeagueController(
    private val leagueService: LeagueService,
    private val waitlistService: WaitlistService,
    private val standingsService: StandingsService,
    private val notificationService: NotificationService
) {
    @Operation(summary = "Create a new league")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF') and #orgId == principal.orgId")
    @PostMapping("/leagues")
    fun createLeague(
        @PathVariable orgId: UUID,
        @Validated @RequestBody request: CreateLeagueRequest
    ): ResponseEntity<LeagueResponse> {
        // TODO: Audit log: create league
        val sample = LeagueResponse(UUID.randomUUID(), request.name, request.description, request.format, request.startTime, request.endTime)
        return ResponseEntity.ok(sample)
    }

    @Operation(summary = "Get league details")
    @GetMapping("/leagues/{id}")
    fun getLeague(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<LeagueResponse> {
        // TODO: Fetch league by id and orgId
        val sample = LeagueResponse(id, "Sample League", "desc", "format", "2025-01-01T00:00:00Z", "2025-01-31T00:00:00Z")
        return ResponseEntity.ok(sample)
    }

    @Operation(summary = "Generate schedule for the league")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF') and #orgId == principal.orgId")
    @PostMapping("/leagues/{id}/generate-schedule")
    fun generateSchedule(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        // TODO: Audit log: generate schedule
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Enter a league")
    @PostMapping("/leagues/{id}/enter")
    fun enterLeague(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @Validated @RequestBody request: EnterLeagueRequest
    ): ResponseEntity<Void> {
        // TODO: Add player to league
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Submit score for a match")
    @PostMapping("/leagues/{id}/submit-score")
    fun submitScore(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @Validated @RequestBody request: SubmitScoreRequest
    ): ResponseEntity<Void> {
        // TODO: Submit score for match
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Get league standings")
    @GetMapping("/leagues/{id}/standings")
    fun getStandings(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @RequestParam method: String?
    ): ResponseEntity<List<StandingRow>> {
        val standings = when (method) {
            "points" -> standingsService.calculatePointPercentageStandings(id)
            else -> standingsService.calculateWinPercentageStandings(id)
        }
        return ResponseEntity.ok(standings)
    }

    @PostMapping("/matches/{matchId}/submit-score")
    fun submitMatchScore(
        @PathVariable orgId: UUID,
        @PathVariable matchId: UUID,
        @Validated @RequestBody request: SubmitScoreRequest
    ): ResponseEntity<Void> {
        leagueService.submitMatchScore(orgId, matchId, request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/waitlist")
    fun joinWaitlist(
        @PathVariable orgId: UUID,
        @Validated @RequestBody request: JoinWaitlistRequest
    ): ResponseEntity<Any> {
        val entry = waitlistService.joinWaitlist(orgId, request)
        return ResponseEntity.ok(entry)
    }

    @PostMapping("/waitlist/{id}/offer-accept")
    fun acceptWaitlistOffer(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @Validated @RequestBody request: AcceptOfferRequest
    ): ResponseEntity<Void> {
        waitlistService.acceptOffer(orgId, id, request)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Import players from a file")
    @PostMapping("/leagues/{id}/import-players")
    fun importPlayers(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Any> {
        val summary = leagueService.importPlayers(orgId, id, file)
        return ResponseEntity.ok(summary)
    }

    @Operation(summary = "Export league results")
    @GetMapping("/leagues/{id}/export-results")
    fun exportResults(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @RequestParam format: String?
    ): ResponseEntity<Any> {
        val export = leagueService.exportResults(orgId, id, format ?: "csv")
        return ResponseEntity.ok(export)
    }

    @GetMapping("/notifications/logs")
    fun getNotificationLogs(
        @PathVariable orgId: UUID
    ): ResponseEntity<List<NotificationLog>> {
        val logs = notificationService.listNotificationLogs(orgId)
        return ResponseEntity.ok(logs)
    }

    @PreAuthorize("hasRole('ADMIN') and #orgId == principal.orgId")
    @PostMapping("/leagues/{id}/force-edit-match")
    fun forceEditMatch(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @Validated @RequestBody request: Any // Replace with actual DTO
    ): ResponseEntity<Void> {
        // TODO: Audit log: force edit match
        return ResponseEntity.ok().build()
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF') and #orgId == principal.orgId")
    @PostMapping("/leagues/{id}/create-bracket")
    fun createBracket(
        @PathVariable orgId: UUID,
        @PathVariable id: UUID,
        @Validated @RequestBody request: Any // Replace with actual DTO
    ): ResponseEntity<Void> {
        // TODO: Audit log: create bracket
        return ResponseEntity.ok().build()
    }

    @PreAuthorize("hasRole('ADMIN') and #orgId == principal.orgId")
    @DeleteMapping("/data-request")
    fun deleteOrgData(
        @PathVariable orgId: UUID
    ): ResponseEntity<Void> {
        // TODO: Data retention policy skeleton
        // TODO: Audit log: data deletion request
        return ResponseEntity.ok().build()
    }
}

@RestController
@RequestMapping("/mobile")
class MobileLeagueController(
    private val leagueService: LeagueService,
    private val notificationService: NotificationService
) {
    @GetMapping("/leagues/active")
    fun getActiveLeagues(): ResponseEntity<List<MobileLeagueSummary>> {
        val leagues = leagueService.getActiveLeaguesForMobile()
        return ResponseEntity.ok(leagues)
    }

    @PostMapping("/matches/{matchId}/checkin")
    fun checkinMatch(
        @PathVariable matchId: UUID,
        @RequestBody request: MobileCheckinRequest
    ): ResponseEntity<MobileActionResult> {
        val result = leagueService.mobileCheckinMatch(matchId, request)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/matches/{matchId}/submit-score")
    fun submitScoreMobile(
        @PathVariable matchId: UUID,
        @RequestBody request: MobileSubmitScoreRequest
    ): ResponseEntity<MobileActionResult> {
        val result = leagueService.mobileSubmitScore(matchId, request)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/push/register")
    fun registerPushToken(
        @RequestBody request: MobilePushRegisterRequest
    ): ResponseEntity<Void> {
        notificationService.registerMobilePushToken(request)
        return ResponseEntity.ok().build()
    }
}
