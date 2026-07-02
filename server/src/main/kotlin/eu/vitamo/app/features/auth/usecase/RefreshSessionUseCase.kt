package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.features.auth.model.AuthException
import eu.vitamo.app.features.auth.model.LoginSession
import eu.vitamo.app.features.auth.service.JWTService
import eu.vitamo.app.features.auth.service.RefreshTokenService
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.features.auth.persistence.refresh.toClientContextOrNull
import kotlin.uuid.Uuid

class RefreshSessionUseCase(
    private val userRepository: UserRepository,
    private val refreshTokenService: RefreshTokenService,
    private val jwtService: JWTService,
) {
    suspend fun refresh(refreshToken: String): LoginSession {
        val tokenEntity = refreshTokenService.findValid(refreshToken)
            ?: throw AuthException.InvalidRefreshToken()

        val userId = tokenEntity.userId.value
        val user = userRepository.findById(userId)
            ?: throw AuthException.InvalidRefreshToken()

        refreshTokenService.revoke(tokenEntity.id)

        val accessToken = jwtService.generateAccessToken(user.id)
        val nextRefreshToken = jwtService.generateRefreshToken()
        refreshTokenService.create(
            authToken = nextRefreshToken,
            userId = user.id,
            context = tokenEntity.toClientContextOrNull(),
        )

        return LoginSession(
            user = user,
            accessToken = accessToken,
            refreshToken = nextRefreshToken,
        )
    }

    suspend fun refreshTokensByUserId(userId: Uuid): LoginSession {
        val user = userRepository.findById(userId)
            ?: throw AuthException.InvalidRefreshToken()

        val accessToken = jwtService.generateAccessToken(user.id)
        val nextRefreshToken = jwtService.generateRefreshToken()
        refreshTokenService.create(
            authToken = nextRefreshToken,
            userId = user.id,
            context = null,
        )

        return LoginSession(
            user = user,
            accessToken = accessToken,
            refreshToken = nextRefreshToken,
        )
    }
}
