package eu.vitamo.app.features.auth.routes

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class AuthRoutesCookieTest {
    @Test
    fun cookieValue_omitsSecureForDevelopment() {
        val cookie = cookieValue("access_token", "token", Clock.System.now() + 1.hours, secure = false)

        assertFalse(cookie.contains("; Secure"))
        assertContains(cookie, "HttpOnly")
        assertContains(cookie, "SameSite=Lax")
    }

    @Test
    fun cookieValue_includesSecureForProduction() {
        val cookie = cookieValue("access_token", "token", Clock.System.now() + 1.hours, secure = true)

        assertTrue(cookie.contains("; Secure"))
    }
}
