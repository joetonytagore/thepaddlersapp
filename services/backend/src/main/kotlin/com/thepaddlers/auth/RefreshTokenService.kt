package com.thepaddlers.auth

import com.thepaddlers.auth.RefreshTokenEntity
import com.thepaddlers.auth.RefreshTokenRepository
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant
import java.util.*

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository
) {
    private val secureRandom = SecureRandom()
    fun create(userId: UUID, expiresInMinutes: Long = 43200): RefreshTokenEntity {
        val token = generateToken()
        val entity = RefreshTokenEntity(
            token = token,
            userId = userId,
            expiresAt = Instant.now().plusSeconds(expiresInMinutes * 60)
        )
        return refreshTokenRepository.save(entity)
    }
    fun validate(token: String): RefreshTokenEntity? {
        val entity = refreshTokenRepository.findByToken(token) ?: return null
        if (entity.revoked || entity.expiresAt.isBefore(Instant.now())) return null
        return entity
    }
    fun rotate(oldToken: String, userId: UUID): Pair<RefreshTokenEntity, RefreshTokenEntity?> {
        val oldEntity = validate(oldToken) ?: return Pair(null, null)
        oldEntity.revoked = true
        refreshTokenRepository.save(oldEntity)
        val newEntity = create(userId)
        return Pair(newEntity, oldEntity)
    }
    fun revoke(token: String) {
        val entity = refreshTokenRepository.findByToken(token) ?: return
        entity.revoked = true
        refreshTokenRepository.save(entity)
    }
    fun revokeAll(userId: UUID) {
        val tokens = refreshTokenRepository.findAllByUserId(userId)
        tokens.forEach {
            it.revoked = true
            refreshTokenRepository.save(it)
        }
    }
    private fun generateToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}

