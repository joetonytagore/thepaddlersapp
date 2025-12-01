package com.thepaddlers.auth

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.*

class RefreshTokenServiceTest {
    private val repo: RefreshTokenRepository = mock()
    private val service = RefreshTokenService(repo)
    private val userId = UUID.randomUUID()

    @Test
    fun `create and validate refresh token`() {
        val token = service.create(userId)
        whenever(repo.findByToken(token.token)).thenReturn(token)
        val validated = service.validate(token.token)
        assertNotNull(validated)
        assertEquals(userId, validated!!.userId)
    }

    @Test
    fun `rotate refresh token revokes old and issues new`() {
        val oldToken = service.create(userId)
        whenever(repo.findByToken(oldToken.token)).thenReturn(oldToken)
        val (newToken, revoked) = service.rotate(oldToken.token, userId)
        assertTrue(revoked!!.revoked)
        assertNotEquals(oldToken.token, newToken.token)
    }

    @Test
    fun `revoke refresh token sets revoked`() {
        val token = service.create(userId)
        whenever(repo.findByToken(token.token)).thenReturn(token)
        service.revoke(token.token)
        assertTrue(token.revoked)
    }
}

