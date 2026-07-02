package eu.vitamo.app.network.auth

import io.ktor.http.Cookie
import kotlinx.serialization.Serializable

@Serializable
data class CookieSnapshot(
    val name: String,
    val value: String,
    val domain: String? = null,
    val path: String? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = false,
)

fun CookieSnapshot.toCookie(): Cookie {
    return Cookie(
        name = name,
        value = value,
        domain = domain,
        path = path,
        secure = secure,
        httpOnly = httpOnly,
    )
}

fun Cookie.toSnapshot(): CookieSnapshot {
    return CookieSnapshot(
        name = name,
        value = value,
        domain = domain,
        path = path,
        secure = secure,
        httpOnly = httpOnly,
    )
}
