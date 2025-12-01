package com.thepaddlers.league.services

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

class StandingsServiceUnitTest {
    private val matchRepository: MatchRepository = mock()
    private val scoreRepository: ScoreRepository = mock()
    private val standingsService = StandingsService(matchRepository, scoreRepository)

    @Test
    fun `win percentage standings calculation`() {
        // ...synthetic data setup as in previous tests...
        // Assert correct win percentage standings
    }

    @Test
    fun `point percentage standings calculation`() {
        // ...synthetic data setup as in previous tests...
        // Assert correct point percentage standings
    }
}

