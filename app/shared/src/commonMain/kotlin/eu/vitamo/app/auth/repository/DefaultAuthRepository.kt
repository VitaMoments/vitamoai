package eu.vitamo.app.auth.repository

import eu.vitamo.app.api.contracts.auth.ForgotPasswordRequest
import eu.vitamo.app.api.contracts.auth.ForgotPasswordResponse
import eu.vitamo.app.api.contracts.auth.LoginRequest
import eu.vitamo.app.api.contracts.auth.RegisterRequest
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationRequest
import eu.vitamo.app.api.contracts.auth.ResetPasswordRequest
import eu.vitamo.app.api.contracts.auth.ResetPasswordResponse
import eu.vitamo.app.api.contracts.auth.VerifyEmailRequest
import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.auth.api.AuthApi
import eu.vitamo.app.mapper.toRepositoryResult
import eu.vitamo.app.network.AuthCookieStorage
import eu.vitamo.app.repository.RepositoryResult

class DefaultAuthRepository(
    private val authApi: AuthApi,
    private val cookieStorage: AuthCookieStorage,
) : AuthRepository {

    override suspend fun register(
        username: String,
        email: String,
        password: String,
    ): RepositoryResult<Unit> {
        return authApi.register(
            RegisterRequest(
                displayName = username,
                email = email,
                password = password,
            )
        ).toRepositoryResult {
            Unit
        }
    }

    override suspend fun login(
        email: String,
        password: String,
    ): RepositoryResult<AuthenticatedUser> {
        return authApi.login(
            LoginRequest(
                email = email,
                password = password,
            )
        ).toRepositoryResult { response ->
            response.user
        }
    }

    override suspend fun verifyEmail(
        email: String,
        code: String,
    ): RepositoryResult<AuthenticatedUser> {
        return authApi.verifyEmail(
            VerifyEmailRequest(
                email = email,
                code = code,
            )
        ).toRepositoryResult { response ->
            response.user as AuthenticatedUser
        }
    }

    override suspend fun resendEmailVerification(
        email: String,
    ): RepositoryResult<Unit> {
        return authApi.resendEmailVerification(
            ResendEmailVerificationRequest(
                email = email,
            )
        ).toRepositoryResult {
            Unit
        }
    }

    override suspend fun currentSessionUser(): RepositoryResult<AuthenticatedUser> {
        return authApi.session().toRepositoryResult { response ->
            response.user
        }
    }

    override suspend fun logout(): RepositoryResult<Unit> {
        val result = authApi.logout()

        cookieStorage.clearAuthCookies()

        return result.toRepositoryResult {
            Unit
        }
    }

    override suspend fun forgotPassword(email: String): RepositoryResult<ForgotPasswordResponse> {
        return authApi.forgotPassword(ForgotPasswordRequest(email = email)).toRepositoryResult { response ->
            response
        }
    }

    override suspend fun resetPassword(
        token: String,
        email: String,
        password: String
    ): RepositoryResult<ResetPasswordResponse> {
        return authApi.resetPassword(ResetPasswordRequest(email = email, token = token, newPassword = password)).toRepositoryResult { response ->
            response
        }
    }
}