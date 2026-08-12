package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.exception.ApiException
import eu.vitamo.app.exception.AuthException
import eu.vitamo.app.features.auth.model.LoginSession
import eu.vitamo.app.features.auth.service.JWTService
import eu.vitamo.app.features.auth.service.RefreshTokenService
import eu.vitamo.app.features.user.mapper.toAuthenticatedUser
import eu.vitamo.app.features.user.model.UserRecord
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid

class RefreshSessionUseCase(
    private val userRepository: UserRepository,
    private val refreshTokenService: RefreshTokenService,
    private val jwtService: JWTService,
) {

    suspend fun refresh(
        refreshToken: String,
    ): LoginSession {
        val currentToken =
            refreshTokenService.findValid(refreshToken)
                ?: throw AuthException.InvalidRefreshToken()

        val userRecord = findUser(
            userId = currentToken.userId.value,
            onNotFound = {
                AuthException.InvalidRefreshToken()
            },
        )

        val deviceId =
            currentToken.deviceId.value

        /*
         * Eerst oude token revoken.
         *
         * Daarna wordt de nieuwe refresh token
         * aan exact hetzelfde device gekoppeld.
         */
        refreshTokenService.revoke(
            currentToken.id,
        )

        return createLoginSession(
            userRecord = userRecord,
            deviceId = deviceId,
        )
    }

    suspend fun createSessionByUserId(
        userId: Uuid,
        deviceId: Uuid,
    ): LoginSession {
        val userRecord = findUser(
            userId = userId,
            onNotFound = {
                AuthException.InvalidAccessToken()
            },
        )

        return createLoginSession(
            userRecord = userRecord,
            deviceId = deviceId,
        )
    }

    private suspend fun findUser(
        userId: Uuid,
        onNotFound: () -> RuntimeException,
    ): UserRecord =
        when (
            val result =
                userRepository.findById(userId)
        ) {
            is RepositoryResult.Success ->
                result.data

            is RepositoryResult.Error ->
                when (result.error) {
                    is RepositoryError.NotFound ->
                        throw onNotFound()

                    else ->
                        throw ApiException.Internal(
                            message = "Failed to retrieve user while creating a login session.",
                        )
                }
        }

    private fun createLoginSession(
        userRecord: UserRecord,
        deviceId: Uuid,
    ): LoginSession {
        val accessToken =
            jwtService.generateAccessToken(
                userId = userRecord.id,
                deviceId = deviceId,
            )

        val refreshToken =
            jwtService.generateRefreshToken()

        refreshTokenService.create(
            authToken = refreshToken,
            userId = userRecord.id,
            deviceId = deviceId,
        )

        return LoginSession(
            user = userRecord.toAuthenticatedUser(null),
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}