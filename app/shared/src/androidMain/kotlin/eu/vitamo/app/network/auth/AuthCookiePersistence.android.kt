package eu.vitamo.app.network.auth

import android.content.Context

private const val AUTH_COOKIE_STORAGE_NAME = "auth_cookie_storage"
private const val AUTH_COOKIE_STORAGE_KEY = "serialized_cookies"

private lateinit var appContext: Context

fun initializeAuthCookiePersistence(context: Context) {
    appContext = context.applicationContext
}

actual fun createAuthCookiePersistence(): AuthCookiePersistence {
    check(::appContext.isInitialized) {
        "Android auth cookie persistence was not initialized"
    }

    val prefs = appContext.getSharedPreferences(AUTH_COOKIE_STORAGE_NAME, Context.MODE_PRIVATE)
    return object : AuthCookiePersistence {
        override fun read(): String? = prefs.getString(AUTH_COOKIE_STORAGE_KEY, null)

        override fun write(serializedCookies: String) {
            prefs.edit().putString(AUTH_COOKIE_STORAGE_KEY, serializedCookies).apply()
        }

        override fun clear() {
            prefs.edit().remove(AUTH_COOKIE_STORAGE_KEY).apply()
        }
    }
}
