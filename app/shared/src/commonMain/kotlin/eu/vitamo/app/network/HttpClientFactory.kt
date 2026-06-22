package eu.vitamo.app.network

import eu.vitamo.app.serialization.AppJson
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.kotlinx.json.json

fun createAppHttpClient(cookieStorage: AuthCookieStorage): HttpClient {
    return HttpClient(platformHttpClientEngineFactory()) {
        install(ContentNegotiation) {
            json(AppJson)
        }
        install(HttpCookies) {
            storage = cookieStorage
        }
    }
}
