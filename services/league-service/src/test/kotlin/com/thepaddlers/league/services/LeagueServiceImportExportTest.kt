package com.thepaddlers.league.services

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.mock.web.MockMultipartFile
import java.util.*

class LeagueServiceImportExportTest {
    private val leagueService = LeagueService(mock(), mock(), mock())

    @Test
    fun `import valid CSV rows`() {
        val csv = "name,email,dupr_rating,partner_email\nAlice,alice@example.com,4.5,bob@example.com\nBob,bob@example.com,4.0,\n"
        val file = MockMultipartFile("file", "players.csv", "text/csv", csv.toByteArray())
        val summary = leagueService.importPlayers(UUID.randomUUID(), UUID.randomUUID(), file)
        assertEquals(2, summary.results.size)
        assertTrue(summary.results.all { it.status == "Imported" })
    }

    @Test
    fun `import invalid CSV rows`() {
        val csv = "name,email,dupr_rating,partner_email\nAlice,invalid-email,4.5,bob@example.com\nBob,bob@example.com,4.0,\nBob,bob@example.com,4.0,\n"
        val file = MockMultipartFile("file", "players.csv", "text/csv", csv.toByteArray())
        val summary = leagueService.importPlayers(UUID.randomUUID(), UUID.randomUUID(), file)
        assertEquals(3, summary.results.size)
        assertEquals("Invalid email", summary.results[0].status)
        assertEquals("Imported", summary.results[1].status)
        assertEquals("Duplicate email", summary.results[2].status)
    }
}

