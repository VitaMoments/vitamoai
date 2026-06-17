package eu.vitamo.app.features.auth.routes

import eu.vitamo.app.api.contracts.auth.LoginRequest
import eu.vitamo.app.api.contracts.auth.LoginResponse
import eu.vitamo.app.api.contracts.auth.RegisterRequest
import eu.vitamo.app.api.contracts.auth.VerifyEmailRequest
import eu.vitamo.app.features.auth.model.LoginSession
import eu.vitamo.app.features.auth.usecase.LoginUseCase
import eu.vitamo.app.features.auth.usecase.RegisterUseCase
import eu.vitamo.app.features.auth.usecase.VerifyEmailUseCase
import eu.vitamo.app.features.user.mapper.toAuthenticatedUser
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.core.context.GlobalContext
import kotlin.time.Clock

fun Route.authRoutes() {
    post("/auth/register") {
        val koin = GlobalContext.get()
        val registerUseCase = koin.get<RegisterUseCase>()
        val request = call.receive<RegisterRequest>()
        val response = registerUseCase.register(request)
        call.respond(HttpStatusCode.Created, response)
    }

    post("/auth/verify-email") {
        val koin = GlobalContext.get()
        val verifyEmailUseCase = koin.get<VerifyEmailUseCase>()
        val request = call.receive<VerifyEmailRequest>()
        call.respond(verifyEmailUseCase.verify(request))
    }

    post("/auth/login") {
        val koin = GlobalContext.get()
        val loginUseCase = koin.get<LoginUseCase>()
        val request = call.receive<LoginRequest>()
        val session = loginUseCase.login(request)
        appendAuthCookies(call, session)
        call.respond(LoginResponse(user = session.user.toAuthenticatedUser()))
    }
}

private fun appendAuthCookies(call: ApplicationCall, session: LoginSession) {
    call.response.headers.append(
        HttpHeaders.SetCookie,
        cookieValue("access_token", session.accessToken.token, session.accessToken.expiresAt),
    )
    call.response.headers.append(
        HttpHeaders.SetCookie,
        cookieValue("refresh_token", session.refreshToken.token, session.refreshToken.expiresAt),
    )
}

private fun cookieValue(name: String, value: String, expiresAt: kotlin.time.Instant): String {
    val maxAge = (expiresAt - Clock.System.now()).inWholeSeconds.coerceAtLeast(0)
    return buildString {
        append(name)
        append('=')
        append(value)
        append("; Path=/; HttpOnly; Secure; SameSite=Lax")
        if (maxAge > 0) {
            append("; Max-Age=")
            append(maxAge)
        }
    }
}
