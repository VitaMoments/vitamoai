package eu.vitamo.app.auth.api

import eu.vitamo.app.api.contracts.auth.LoginRequest
import eu.vitamo.app.api.contracts.auth.LoginResponse
import eu.vitamo.app.api.contracts.auth.RegisterRequest
import eu.vitamo.app.api.contracts.auth.RegisterResponse
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationRequest
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationResponse
import eu.vitamo.app.api.contracts.auth.VerifyEmailRequest
import eu.vitamo.app.api.contracts.auth.VerifyEmailResponse
import eu.vitamo.app.api.contracts.user.AuthenticatedUser

interface AuthApi {
    suspend fun register(request: RegisterRequest): RegisterResponse
    suspend fun login(request: LoginRequest): LoginResponse
    suspend fun verifyEmail(request: VerifyEmailRequest): VerifyEmailResponse
    suspend fun resendEmailVerification(request: ResendEmailVerificationRequest): ResendEmailVerificationResponse
    suspend fun session(): AuthenticatedUser?
    suspend fun logout()
}

