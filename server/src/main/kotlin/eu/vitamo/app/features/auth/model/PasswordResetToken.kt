package eu.vitamo.app.features.auth.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class PasswordResetToken(
    val id: Uuid,
    val userId: Uuid,
    val tokenHash: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val consumedAt: Instant?,
    val attempts: Int,
    val lastAttemptAt: Instant?,
)
