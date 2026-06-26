package eu.vitamo.app.auth.api

import eu.vitamo.app.api.contracts.auth.ForgotPasswordRequest
import eu.vitamo.app.api.contracts.auth.ForgotPasswordResponse
import eu.vitamo.app.api.contracts.auth.LoginRequest
import eu.vitamo.app.api.contracts.auth.LoginResponse
import eu.vitamo.app.api.contracts.auth.RegisterRequest
import eu.vitamo.app.api.contracts.auth.RegisterResponse
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationRequest
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationResponse
import eu.vitamo.app.api.contracts.auth.ResetPasswordRequest
import eu.vitamo.app.api.contracts.auth.ResetPasswordResponse
import eu.vitamo.app.api.contracts.auth.SessionResponse
import eu.vitamo.app.api.contracts.auth.VerifyEmailRequest
import eu.vitamo.app.api.contracts.auth.VerifyEmailResponse
import eu.vitamo.app.api.contracts.user.AuthenticatedUser
import eu.vitamo.app.api.result.ApiResult

interface AuthApi {
    suspend fun register(request: RegisterRequest): ApiResult<RegisterResponse>

    suspend fun login(request: LoginRequest): ApiResult<LoginResponse>

    suspend fun verifyEmail(request: VerifyEmailRequest): ApiResult<VerifyEmailResponse>

    suspend fun resendEmailVerification(
        request: ResendEmailVerificationRequest,
    ): ApiResult<ResendEmailVerificationResponse>

    suspend fun session(): ApiResult<SessionResponse>

    suspend fun logout(): ApiResult<Unit>

    suspend fun forgotPassword(request: ForgotPasswordRequest): ApiResult<ForgotPasswordResponse>
    suspend fun resetPassword(request: ResetPasswordRequest): ApiResult<ResetPasswordResponse>
}

