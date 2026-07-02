package eu.vitamo.app.network.auth

import eu.vitamo.app.network.AuthCookieStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class PersistentCookieStorage(
    private val persistence: AuthCookiePersistence,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : AuthCookieStorage {
    private data class StoredCookie(
        val cookie: Cookie,
    )

    private val lock = Mutex()
    private val cookies = mutableListOf<StoredCookie>()

    init {
        val serialized = persistence.read()
        if (!serialized.isNullOrBlank()) {
            val snapshots = json.decodeFromString(
                ListSerializer(CookieSnapshot.serializer()),
                serialized,
            )
            cookies += snapshots.map { snapshot ->
                StoredCookie(snapshot.toCookie())
            }
        }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        lock.withLock {
            cookies.removeAll { existing ->
                existing.cookie.name == cookie.name &&
                    existing.cookie.domain == cookie.domain &&
                    existing.cookie.path == cookie.path
            }
            cookies += StoredCookie(cookie)
            persistLocked()
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
            persistLocked()
        }
    }

    override suspend fun clearAllCookies() {
        lock.withLock {
            cookies.clear()
            persistence.clear()
        }
    }

    suspend fun clear() {
        clearAllCookies()
    }

    private fun persistLocked() {
        val serialized = json.encodeToString(
            ListSerializer(CookieSnapshot.serializer()),
            cookies.map { it.cookie.toSnapshot() },
        )
        persistence.write(serialized)
    }

    override fun close() = Unit
}
