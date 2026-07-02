package eu.vitamo.app.features.auth.routes

import eu.vitamo.app.api.contracts.auth.ForgotPasswordRequest
import eu.vitamo.app.api.contracts.auth.LoginRequest
import eu.vitamo.app.api.contracts.auth.LoginResponse
import eu.vitamo.app.api.contracts.auth.RegisterRequest
import eu.vitamo.app.api.contracts.auth.ResendEmailVerificationRequest
import eu.vitamo.app.api.contracts.auth.ResetPasswordRequest
import eu.vitamo.app.api.contracts.auth.SessionResponse
import eu.vitamo.app.api.contracts.auth.VerifyEmailRequest
import eu.vitamo.app.features.auth.model.AuthException
import eu.vitamo.app.features.auth.model.LoginSession
import eu.vitamo.app.features.auth.usecase.ForgotPasswordUseCase
import eu.vitamo.app.features.auth.usecase.LoginUseCase
import eu.vitamo.app.features.auth.usecase.RegisterUseCase
import eu.vitamo.app.features.auth.usecase.ResendEmailVerificationUseCase
import eu.vitamo.app.features.auth.usecase.RefreshSessionUseCase
import eu.vitamo.app.features.auth.usecase.ResetPasswordUseCase
import eu.vitamo.app.features.auth.usecase.VerifyEmailUseCase
import eu.vitamo.app.features.user.mapper.toAuthenticatedUser
import eu.vitamo.app.config.AuthCookieConfig
import eu.vitamo.app.infrastructure.network.helpers.requireUserId
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import kotlin.time.Clock

fun Route.authRoutes() {
    val registerUseCase: RegisterUseCase by inject()
    val verifyEmailUseCase: VerifyEmailUseCase by inject()
    val resendEmailVerificationUseCase: ResendEmailVerificationUseCase by inject()
    val loginUseCase: LoginUseCase by inject()
    val refreshSessionUseCase: RefreshSessionUseCase by inject()
    val forgotPasswordUseCase: ForgotPasswordUseCase by inject()
    val resetPasswordUseCase: ResetPasswordUseCase by inject()
    val authCookieConfig: AuthCookieConfig by inject()

    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = registerUseCase.register(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/verify-email") {
            val request = call.receive<VerifyEmailRequest>()
            call.respond(verifyEmailUseCase.verify(request))
        }

        post("/resend-email-verification") {
            val request = call.receive<ResendEmailVerificationRequest>()
            val response = resendEmailVerificationUseCase.resend(request)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val session = loginUseCase.login(request)
            appendAuthCookies(call, session, authCookieConfig)
            call.respond(
                LoginResponse(
                    user = session.user.toAuthenticatedUser()
                )
            )
        }

        post("/refresh") {
            val refreshToken = call.request.cookies["refresh_token"]
                ?: throw AuthException.InvalidRefreshToken()
            val session = refreshSessionUseCase.refresh(refreshToken)
            appendAuthCookies(call, session, authCookieConfig)
            call.respond(
                SessionResponse(
                    user = session.user.toAuthenticatedUser()
                )
            )
        }

        post("/reset-password") {
            val request: ResetPasswordRequest =  call.receive<ResetPasswordRequest>()
            val response = resetPasswordUseCase(request)

            call.respond(status = HttpStatusCode.OK, message = response)
        }

        post("/forgot-password") {
            val request = call.receive<ForgotPasswordRequest>()
            val response = forgotPasswordUseCase(request)

            call.respond(
                status = HttpStatusCode.OK,
                message = response,
            )
        }

        authenticate("cookie-jwt-authentication") {
            post("/logout") {
                call.response.headers.append(
                    HttpHeaders.SetCookie,
                    cookieValue("access_token", "", expiresAt = Clock.System.now(), secure = authCookieConfig.secure),
                )
                call.response.headers.append(
                    HttpHeaders.SetCookie,
                    cookieValue("refresh_token", "", expiresAt = Clock.System.now(), secure = authCookieConfig.secure),
                )
                call.respond(HttpStatusCode.OK)
            }

            get("/session") {
                val userId = call.requireUserId()
                val session = refreshSessionUseCase.refreshTokensByUserId(userId)
                appendAuthCookies(call, session, authCookieConfig)
                call.respond(
                    SessionResponse(
                        user = session.user.toAuthenticatedUser()
                    )
                )
            }

        }
    }
}

internal fun appendAuthCookies(call: ApplicationCall, session: LoginSession, authCookieConfig: AuthCookieConfig) {
    call.response.headers.append(
        HttpHeaders.SetCookie,
        cookieValue("access_token", session.accessToken.token, session.accessToken.expiresAt, authCookieConfig.secure),
    )
    call.response.headers.append(
        HttpHeaders.SetCookie,
        cookieValue("refresh_token", session.refreshToken.token, session.refreshToken.expiresAt, authCookieConfig.secure),
    )
}

internal fun cookieValue(name: String, value: String, expiresAt: kotlin.time.Instant, secure: Boolean): String {
    val maxAge = (expiresAt - Clock.System.now()).inWholeSeconds.coerceAtLeast(0)
    return buildString {
        append(name)
        append('=')
        append(value)
        append("; Path=/; HttpOnly")
        if (secure) {
            append("; Secure")
        }
        append("; SameSite=Lax")
        if (maxAge > 0) {
            append("; Max-Age=")
            append(maxAge)
        }
    }
}
