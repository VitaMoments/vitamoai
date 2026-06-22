package eu.vitamo.app.network

import io.ktor.client.plugins.cookies.CookiesStorage

interface AuthCookieStorage : CookiesStorage {
    suspend fun clearAuthCookies()
    suspend fun clearAllCookies()
}

