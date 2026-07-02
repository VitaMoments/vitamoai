package eu.vitamo.app.features.auth.repository

import eu.vitamo.app.features.auth.model.PasswordResetTokenRecord
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface PasswordResetTokenRepository {
    suspend fun create(
        userId: Uuid,
        tokenHash: String,
        createdAt: Instant,
        expiresAt: Instant,
    ): PasswordResetTokenRecord

    suspend fun findByTokenHash(tokenHash: String): PasswordResetTokenRecord?

    suspend fun findLatestByUserId(userId: Uuid): PasswordResetTokenRecord?

    suspend fun consumeIfActive(
        tokenId: Uuid,
        consumedAt: Instant,
    ): Boolean

    fun markConsumed(id: Uuid, consumedAt: Instant)

    suspend fun consumeActiveForUser(
        userId: Uuid,
        consumedAt: Instant,
    )

    suspend fun incrementAttempts(
        tokenId: Uuid,
        attemptedAt: Instant,
    )
}