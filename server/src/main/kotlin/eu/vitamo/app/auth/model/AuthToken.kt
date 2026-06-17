package eu.vitamo.app.auth.model

import kotlin.time.Instant

data class AuthToken(
    val token: String,
    val expiresAt: Instant,
    val type: AuthTokenType,
)
