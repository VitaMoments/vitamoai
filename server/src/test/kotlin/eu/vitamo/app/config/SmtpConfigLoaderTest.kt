package eu.vitamo.app.config

import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SmtpConfigLoaderTest {
    @Test
    fun loadOrThrow_usesDefaultsAndRequiredValues() {
        val config = SmtpConfigLoader.loadOrThrow(
            valueReader = reader(
                env = mapOf(
                    "SMTP_HOST" to "smtp.example.com",
                    "SMTP_USERNAME" to "mailer@example.com",
                    "SMTP_PASSWORD" to "secret",
                ),
            ),
        )

        assertEquals("smtp.example.com", config.host)
        assertEquals(465, config.port)
        assertEquals("mailer@example.com", config.username)
        assertEquals("secret", config.password)
        assertEquals("mailer@example.com", config.fromAddress)
        assertEquals("VitaMo", config.fromName)
        assertTrue(config.sslEnabled)
        assertFalse(config.startTlsEnabled)
        assertTrue(config.authEnabled)
    }

    @Test
    fun loadOrThrow_prefersEnvironmentThenSystemProperties() {
        val systemProperties = Properties().apply {
            setProperty("SMTP_HOST", "system-host")
            setProperty("SMTP_USERNAME", "system-user")
            setProperty("SMTP_PASSWORD", "system-password")
            setProperty("SMTP_FROM_NAME", "System")
        }

        val config = SmtpConfigLoader.loadOrThrow(
            valueReader = reader(
                env = mapOf(
                "SMTP_HOST" to "env-host",
                "SMTP_USERNAME" to "env-user",
                "SMTP_PASSWORD" to "env-password",
                "SMTP_PORT" to "465",
                ),
                system = systemProperties,
            ),
        )

        assertEquals("env-host", config.host)
        assertEquals(465, config.port)
        assertEquals("env-user", config.username)
        assertEquals("env-password", config.password)
        assertEquals("env-user", config.fromAddress)
        assertEquals("System", config.fromName)
        assertTrue(config.sslEnabled)
        assertFalse(config.startTlsEnabled)
        assertTrue(config.authEnabled)
    }

    @Test
    fun loadOrThrow_usesSystemPropertiesWhenEnvironmentIsMissing() {
        val config = SmtpConfigLoader.loadOrThrow(
            valueReader = reader(
                system = Properties().apply {
                setProperty("SMTP_HOST", "system-host")
                setProperty("SMTP_USERNAME", "system-user")
                setProperty("SMTP_PASSWORD", "system-password")
                setProperty("SMTP_PORT", "587")
                },
            ),
        )

        assertEquals("system-host", config.host)
        assertEquals(587, config.port)
        assertEquals("system-user", config.username)
        assertEquals("system-password", config.password)
        assertEquals("system-user", config.fromAddress)
        assertFalse(config.sslEnabled)
        assertTrue(config.startTlsEnabled)
        assertTrue(config.authEnabled)
    }

    private fun reader(
        env: Map<String, String> = emptyMap(),
        system: Properties = Properties(),
    ): (String) -> String? = { key ->
        env[key] ?: system.getProperty(key)
    }
}
