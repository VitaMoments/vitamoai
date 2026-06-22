package eu.vitamo.app.network

import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ClearableCookieStorage : AuthCookieStorage {
    private data class StoredCookie(
        val requestUrl: Url,
        val cookie: Cookie,
    )

    private val lock = Mutex()
    private val cookies = mutableListOf<StoredCookie>()

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        lock.withLock {
            cookies.removeAll { existing ->
                existing.cookie.name == cookie.name &&
                    existing.cookie.domain == cookie.domain &&
                    existing.cookie.path == cookie.path
            }
            cookies += StoredCookie(requestUrl, cookie)
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> = lock.withLock {
        cookies.map { it.cookie }.filter { cookie ->
            if (cookie.secure && requestUrl.protocol.name != "https") {
                return@filter false
            }
            val cookieDomain = cookie.domain?.removePrefix(".")
            if (cookieDomain != null && !requestUrl.host.endsWith(cookieDomain)) {
                return@filter false
            }
            val cookiePath = cookie.path ?: "/"
            requestUrl.encodedPath.startsWith(cookiePath)
        }
    }

    override suspend fun clearAuthCookies() {
        lock.withLock {
            cookies.removeAll { it.cookie.name == "access_token" || it.cookie.name == "refresh_token" }
        }
    }

    override suspend fun clearAllCookies() {
        lock.withLock {
            cookies.clear()
        }
    }

    suspend fun clear() {
        clearAllCookies()
    }

    override fun close() = Unit
}
