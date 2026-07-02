package eu.vitamo.app.network.auth

import platform.Foundation.NSUserDefaults

private const val AUTH_COOKIE_STORAGE_KEY = "eu.vitamo.app.auth.cookies"

actual fun createAuthCookiePersistence(): AuthCookiePersistence {
    val defaults = NSUserDefaults.standardUserDefaults
    return object : AuthCookiePersistence {
        override fun read(): String? = defaults.stringForKey(AUTH_COOKIE_STORAGE_KEY)

        override fun write(serializedCookies: String) {
            defaults.setObject(serializedCookies, forKey = AUTH_COOKIE_STORAGE_KEY)
        }

        override fun clear() {
            defaults.removeObjectForKey(AUTH_COOKIE_STORAGE_KEY)
        }
    }
}
