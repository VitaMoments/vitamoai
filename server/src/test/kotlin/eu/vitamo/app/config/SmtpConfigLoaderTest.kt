package eu.vitamo.app.config

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SmtpConfigLoaderTest {
    @Test
    fun loadOrThrow_usesDefaultsAndRequiredValues() {
        val config = SmtpConfigLoader.loadOrThrow(
            environment = mapOf(
                "SMTP_HOST" to "smtp.example.com",
                "SMTP_USERNAME" to "mailer@example.com",
                "SMTP_PASSWORD" to "secret",
            ),
            systemProperties = Properties(),
            dotEnvPath = missingDotEnv(),
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
    fun loadOrThrow_prefersEnvironmentThenSystemPropertiesThenDotEnv() {
        val dotEnvPath = Files.createTempFile("smtp-config", ".env")
        Files.writeString(
            dotEnvPath,
            """
            SMTP_HOST=dot-env-host
            SMTP_USERNAME=dot-env-user
            SMTP_PASSWORD=dot-env-password
            SMTP_PORT=587
            SMTP_FROM_ADDRESS=dot-env-from@example.com
            SMTP_FROM_NAME=Dot Env
            SMTP_SSL_ENABLED=false
            SMTP_STARTTLS_ENABLED=false
            SMTP_AUTH_ENABLED=false
            """.trimIndent(),
            StandardCharsets.UTF_8,
        )

        val systemProperties = Properties().apply {
            setProperty("SMTP_HOST", "system-host")
            setProperty("SMTP_USERNAME", "system-user")
            setProperty("SMTP_PASSWORD", "system-password")
            setProperty("SMTP_FROM_NAME", "System")
        }

        val config = SmtpConfigLoader.loadOrThrow(
            environment = mapOf(
                "SMTP_HOST" to "env-host",
                "SMTP_USERNAME" to "env-user",
                "SMTP_PASSWORD" to "env-password",
                "SMTP_PORT" to "465",
            ),
            systemProperties = systemProperties,
            dotEnvPath = dotEnvPath,
        )

        assertEquals("env-host", config.host)
        assertEquals(465, config.port)
        assertEquals("env-user", config.username)
        assertEquals("env-password", config.password)
        assertEquals("dot-env-from@example.com", config.fromAddress)
        assertEquals("System", config.fromName)
        assertFalse(config.sslEnabled)
        assertFalse(config.startTlsEnabled)
        assertFalse(config.authEnabled)

        Files.deleteIfExists(dotEnvPath)
    }

    @Test
    fun loadOrThrow_usesDotEnvWhenEnvironmentAndSystemPropertiesAreMissing() {
        val dotEnvPath = Files.createTempFile("smtp-config", ".env")
        Files.writeString(
            dotEnvPath,
            """
            SMTP_HOST=dot-env-host
            SMTP_USERNAME=dot-env-user
            SMTP_PASSWORD=dot-env-password
            SMTP_PORT=587
            """.trimIndent(),
            StandardCharsets.UTF_8,
        )

        val config = SmtpConfigLoader.loadOrThrow(
            environment = emptyMap(),
            systemProperties = Properties(),
            dotEnvPath = dotEnvPath,
        )

        assertEquals("dot-env-host", config.host)
        assertEquals(587, config.port)
        assertEquals("dot-env-user", config.username)
        assertEquals("dot-env-password", config.password)
        assertEquals("dot-env-user", config.fromAddress)
        assertFalse(config.sslEnabled)
        assertTrue(config.startTlsEnabled)
        assertTrue(config.authEnabled)

        Files.deleteIfExists(dotEnvPath)
    }

    private fun missingDotEnv(): Path {
        val path = Files.createTempDirectory("missing-dotenv").resolve(".env")
        Files.deleteIfExists(path)
        return path
    }
}
