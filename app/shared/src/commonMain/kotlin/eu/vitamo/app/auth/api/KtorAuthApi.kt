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
import eu.vitamo.app.network.helper.safeApiCall
import eu.vitamo.app.network.helper.safeApiUnitCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorAuthApi(
    private val client: HttpClient,
    private val config: AuthApiConfig,
) : AuthApi {

    override suspend fun register(
        request: RegisterRequest,
    ): ApiResult<RegisterResponse> {
        return safeApiCall {
            client.post("${config.baseUrl}/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun login(
        request: LoginRequest,
    ): ApiResult<LoginResponse> {
        return safeApiCall {
            client.post("${config.baseUrl}/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun verifyEmail(
        request: VerifyEmailRequest,
    ): ApiResult<VerifyEmailResponse> {
        return safeApiCall {
            client.post("${config.baseUrl}/verify-email") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun resendEmailVerification(
        request: ResendEmailVerificationRequest,
    ): ApiResult<ResendEmailVerificationResponse> {
        return safeApiCall {
            client.post("${config.baseUrl}/resend-email-verification") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun session(): ApiResult<SessionResponse> {
        return safeApiCall {
            client.get("${config.baseUrl}/session")
        }
    }

    override suspend fun refreshSession(): ApiResult<SessionResponse> {
        return safeApiCall {
            client.post("${config.baseUrl}/refresh")
        }
    }

    override suspend fun logout(): ApiResult<Unit> {
        return safeApiUnitCall {
            client.post("${config.baseUrl}/logout")
        }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): ApiResult<ForgotPasswordResponse> {
        return safeApiCall {
            client.post("${config.baseUrl}/forgot-password") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun resetPassword(
        request: ResetPasswordRequest
    ): ApiResult<ResetPasswordResponse> {
        return safeApiCall {
            client.post("${config.baseUrl}/reset-password") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }
}