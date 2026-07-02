package eu.vitamo.app.config

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthCookieConfigTest {
    @Test
    fun fromEnvironment_usesDevelopmentDefaults() {
        val config = AuthCookieConfig.fromEnvironment(environment = emptyMap())

        assertFalse(config.secure)
    }

    @Test
    fun fromEnvironment_usesProductionForProductionModes() {
        val config = AuthCookieConfig.fromEnvironment(
            environment = mapOf("VITAMO_ENV" to "production"),
        )

        assertTrue(config.secure)
    }

    @Test
    fun fromEnvironment_allowsExplicitOverride() {
        val config = AuthCookieConfig.fromEnvironment(
            environment = mapOf(
                "VITAMO_ENV" to "production",
                "AUTH_COOKIE_SECURE" to "false",
            ),
        )

        assertFalse(config.secure)
    }
}
