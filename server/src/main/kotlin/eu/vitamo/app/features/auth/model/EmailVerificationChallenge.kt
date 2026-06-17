package eu.vitamo.app.features.auth.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

data class EmailVerificationChallenge(
    val id: Uuid,
    val userId: Uuid,
    val email: String,
    val codeHash: String,
    val purpose: EmailVerificationPurpose,
    val createdAt: Instant,
    val expiresAt: Instant,
    val consumedAt: Instant?,
    val attempts: Int,
    val lastAttemptAt: Instant?,
)
