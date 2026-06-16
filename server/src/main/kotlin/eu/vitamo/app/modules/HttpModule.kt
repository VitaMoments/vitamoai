package eu.vitamo.app.modules

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import java.net.URI

private const val FRONTEND_HOST_ENV = "FRONTEND_HOST"
private const val HTTPS_SCHEME = "https"
private const val HTTP_SCHEME = "http"
private const val X_ENGINE_HEADER = "X-Engine"
private const val X_ENGINE_VALUE = "Ktor"
private const val X_FRAME_OPTIONS_HEADER = "X-Frame-Options"
private const val X_FRAME_OPTIONS_VALUE = "DENY"
private const val X_CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options"
private const val X_CONTENT_TYPE_OPTIONS_VALUE = "nosniff"

private val devFrontendHosts = listOf(
    "localhost:5173",
    "localhost:5174",
    "localhost:5175",
    "localhost:5176",
)

private val productionFrontendHost = "vitamo.github.io"

fun Application.configureHTTP() {
    install(DefaultHeaders) {
        header(X_FRAME_OPTIONS_HEADER, X_FRAME_OPTIONS_VALUE)
        header(X_CONTENT_TYPE_OPTIONS_HEADER, X_CONTENT_TYPE_OPTIONS_VALUE)
        header(X_ENGINE_HEADER, X_ENGINE_VALUE)
    }

    install(ForwardedHeaders)
    install(XForwardedHeaders)

    install(Compression) {
        gzip()
        deflate()
    }

    install(CORS) {
        allowCredentials = true

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)

        exposeHeader(HttpHeaders.ContentDisposition)
        maxAgeInSeconds = 3600

        devFrontendHosts.forEach { host ->
            allowHost(host, schemes = listOf(HTTP_SCHEME))
        }

        allowHost(productionFrontendHost, schemes = listOf(HTTPS_SCHEME))

        frontendHostFromEnvironment(System.getenv())?.let { host ->
            allowHost(host, schemes = listOf(HTTPS_SCHEME))
        }
    }

    install(AutoHeadResponse)
    install(ConditionalHeaders)
}

internal fun frontendHostFromEnvironment(environment: Map<String, String>): String? {
    val rawValue = environment[FRONTEND_HOST_ENV]?.trim().orEmpty()
    if (rawValue.isBlank()) {
        return null
    }

    return normalizeFrontendHost(rawValue)
}

internal fun normalizeFrontendHost(rawValue: String): String? {
    val value = rawValue.trim()
    if (value.isBlank() || value.any(Char::isWhitespace)) {
        return null
    }

    return if (value.contains("://")) {
        runCatching { URI(value) }
            .getOrNull()
            ?.takeIf { uri ->
                uri.scheme == HTTP_SCHEME || uri.scheme == HTTPS_SCHEME
            }
            ?.takeIf { uri ->
                uri.path.isNullOrBlank() && uri.query.isNullOrBlank() && uri.fragment.isNullOrBlank()
            }
            ?.let { uri ->
                val host = uri.host?.takeIf { it.isNotBlank() } ?: return@let null
                val port = uri.port
                if (port > -1) {
                    "$host:$port"
                } else {
                    host
                }
            }
    } else {
        value.takeIf { it.isNotBlank() && !it.contains('/') && !it.contains('?') && !it.contains('#') }
    }
}




