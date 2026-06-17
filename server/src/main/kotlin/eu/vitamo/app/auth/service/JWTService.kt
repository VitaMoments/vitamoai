package eu.vitamo.app.auth.service

import com.auth0.jwt.JWT
import eu.vitamo.app.auth.config.JWTConfig
import eu.vitamo.app.auth.model.AuthToken
import eu.vitamo.app.auth.model.AuthTokenType
import java.util.Date
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class JWTService(
    private val jwtConfig: JWTConfig,
) {
    fun generateAccessToken(userId: Uuid): AuthToken {
        val issuedAt = Clock.System.now()
        val expiresAt = issuedAt + jwtConfig.accessTokenExpirationSeconds.seconds
        val token = JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(JWTConfig.JWT_SUBJECT)
            .withClaim(JWTConfig.USER_ID_CLAIM, userId.toString())
            .withIssuedAt(Date(issuedAt.toEpochMilliseconds()))
            .withExpiresAt(Date(expiresAt.toEpochMilliseconds()))
            .sign(jwtConfig.algorithm)

        return AuthToken(
            token = token,
            expiresAt = expiresAt,
            type = AuthTokenType.ACCESS,
        )
    }

    fun generateRefreshToken(): AuthToken {
        val expiresAt = Clock.System.now() + jwtConfig.refreshTokenExpirationSeconds.seconds
        return AuthToken(
            token = UUID.randomUUID().toString(),
            expiresAt = expiresAt,
            type = AuthTokenType.REFRESH,
        )
    }
}
