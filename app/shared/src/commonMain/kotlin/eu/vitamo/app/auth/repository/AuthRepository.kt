package eu.vitamo.app.auth.repository

import eu.vitamo.app.api.contracts.auth.ForgotPasswordResponse
import eu.vitamo.app.api.contracts.auth.ResetPasswordResponse
import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.repository.RepositoryResult

interface AuthRepository {
    suspend fun register(
        username: String,
        email: String,
        password: String,
    ): RepositoryResult<Unit>

    suspend fun login(
        email: String,
        password: String,
    ): RepositoryResult<AuthenticatedUser>

    suspend fun verifyEmail(
        email: String,
        code: String,
    ): RepositoryResult<AuthenticatedUser>

    suspend fun resendEmailVerification(
        email: String,
    ): RepositoryResult<Unit>

    suspend fun currentSessionUser(): RepositoryResult<AuthenticatedUser>

    suspend fun logout(): RepositoryResult<Unit>

    suspend fun forgotPassword(
        email: String,
    ): RepositoryResult<ForgotPasswordResponse>

    suspend fun resetPassword(
        token: String,
        email: String,
        password: String,
    ): RepositoryResult<ResetPasswordResponse>
}
