package eu.vitamo.app.network.auth

import java.util.prefs.Preferences

private const val AUTH_COOKIE_STORAGE_KEY = "serialized_cookies"

actual fun createAuthCookiePersistence(): AuthCookiePersistence {
    val prefs = Preferences.userNodeForPackage(AuthCookiePersistence::class.java)
    return object : AuthCookiePersistence {
        override fun read(): String? = prefs.get(AUTH_COOKIE_STORAGE_KEY, null)

        override fun write(serializedCookies: String) {
            prefs.put(AUTH_COOKIE_STORAGE_KEY, serializedCookies)
        }

        override fun clear() {
            prefs.remove(AUTH_COOKIE_STORAGE_KEY)
        }
    }
}
