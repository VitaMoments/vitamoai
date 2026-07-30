package eu.vitamo.app.features.auth.usecase

import eu.vitamo.app.auth.ClientContext
import eu.vitamo.app.exception.ApiException
import eu.vitamo.app.exception.AuthException
import eu.vitamo.app.features.auth.model.LoginSession
import eu.vitamo.app.features.auth.persistence.refresh.toClientContextOrNull
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
        val currentToken = refreshTokenService.findValid(refreshToken)
            ?: throw AuthException.InvalidRefreshToken()

        val userRecord = findUser(
            userId = currentToken.userId.value,
            onNotFound = {
                AuthException.InvalidRefreshToken()
            },
        )

        val session = createLoginSession(
            userRecord = userRecord,
            context = currentToken.toClientContextOrNull(),
        )

        refreshTokenService.revoke(currentToken.id)

        return session
    }

    suspend fun createSessionByUserId(
        userId: Uuid,
    ): LoginSession {
        val userRecord = findUser(
            userId = userId,
            onNotFound = {
                AuthException.InvalidAccessToken()
            },
        )

        return createLoginSession(
            userRecord = userRecord,
            context = null,
        )
    }

    private suspend fun findUser(
        userId: Uuid,
        onNotFound: () -> RuntimeException,
    ): UserRecord =
        when (val result = userRepository.findById(userId)) {
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

    private suspend fun createLoginSession(
        userRecord: UserRecord,
        context: ClientContext?,
    ): LoginSession {
        val accessToken = jwtService.generateAccessToken(userRecord.id)
        val refreshToken = jwtService.generateRefreshToken()

        refreshTokenService.create(
            authToken = refreshToken,
            userId = userRecord.id,
            context = context,
        )

        return LoginSession(
            user = userRecord.toAuthenticatedUser(null),
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}