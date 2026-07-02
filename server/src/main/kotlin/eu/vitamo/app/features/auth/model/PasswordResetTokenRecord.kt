package eu.vitamo.app.features.auth.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class PasswordResetTokenRecord(
    val id: Uuid,
    val userId: Uuid,
    val tokenHash: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val consumedAt: Instant?,
    val attempts: Int,
    val lastAttemptAt: Instant?,
) {
    val isConsumed: Boolean
        get() = consumedAt != null

    fun isExpired(now: Instant): Boolean {
        return expiresAt < now
    }

    fun hasTooManyAttempts(maxAttempts: Int = 5): Boolean {
        return attempts >= maxAttempts
    }

    fun isActive(now: Instant): Boolean {
        return !isConsumed && !isExpired(now) && !hasTooManyAttempts()
    }
}
