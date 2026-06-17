package eu.vitamo.app.features.auth.model

import io.ktor.http.HttpStatusCode

class AuthException(
    val code: String,
    override val message: String,
    val status: HttpStatusCode,
) : RuntimeException(message)
