package eu.vitamo.app.features.auth.repository

import eu.vitamo.app.features.auth.model.EmailVerificationChallenge
import eu.vitamo.app.features.auth.model.EmailVerificationPurpose
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface EmailVerificationChallengeRepository {
    fun create(
        userId: Uuid,
        email: String,
        codeHash: String,
        purpose: EmailVerificationPurpose,
        createdAt: Instant,
        expiresAt: Instant,
    ): EmailVerificationChallenge

    fun findLatestActive(
        email: String,
        purpose: EmailVerificationPurpose,
        now: Instant,
    ): EmailVerificationChallenge?

    fun deleteById(id: Uuid)

    fun markConsumed(id: Uuid, consumedAt: Instant)

    fun incrementAttempts(id: Uuid, attemptedAt: Instant)
}
