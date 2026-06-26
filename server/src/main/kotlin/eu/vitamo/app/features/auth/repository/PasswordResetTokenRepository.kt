package eu.vitamo.app.features.auth.repository

import eu.vitamo.app.features.auth.entity.EmailVerificationChallengeEntity
import eu.vitamo.app.features.auth.entity.PasswordResetTokenEntity
import eu.vitamo.app.features.user.entity.UserEntity
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface PasswordResetTokenRepository {
    suspend fun create(
        user: UserEntity,
        tokenHash: String,
        createdAt: Instant,
        expiresAt: Instant,
    ): PasswordResetTokenEntity

    suspend fun findByTokenHash(tokenHash: String): PasswordResetTokenEntity?

    suspend fun findLatestByUserId(userId: Uuid): PasswordResetTokenEntity?

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