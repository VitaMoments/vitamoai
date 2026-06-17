package eu.vitamo.app.config

import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

object SmtpConfigLoader {
    private const val SMTP_HOST = "SMTP_HOST"
    private const val SMTP_PORT = "SMTP_PORT"
    private const val SMTP_USERNAME = "SMTP_USERNAME"
    private const val SMTP_PASSWORD = "SMTP_PASSWORD"
    private const val SMTP_FROM_ADDRESS = "SMTP_FROM_ADDRESS"
    private const val SMTP_FROM_NAME = "SMTP_FROM_NAME"
    private const val SMTP_SSL_ENABLED = "SMTP_SSL_ENABLED"
    private const val SMTP_STARTTLS_ENABLED = "SMTP_STARTTLS_ENABLED"
    private const val SMTP_AUTH_ENABLED = "SMTP_AUTH_ENABLED"

    fun loadOrThrow(
        environment: Map<String, String> = System.getenv(),
        systemProperties: Properties = System.getProperties(),
        dotEnvPath: Path = Path.of(".env"),
    ): SmtpConfig {
        val dotEnvValues = loadDotEnv(dotEnvPath)

        val host = readRequired(SMTP_HOST, environment, systemProperties, dotEnvValues)
        val username = readRequired(SMTP_USERNAME, environment, systemProperties, dotEnvValues)
        val password = readRequired(SMTP_PASSWORD, environment, systemProperties, dotEnvValues)

        val port = readOptional(
            key = SMTP_PORT,
            environment = environment,
            systemProperties = systemProperties,
            dotEnvValues = dotEnvValues,
            defaultValue = "465",
        ).toIntOrNull() ?: error("Invalid value for $SMTP_PORT")

        val fromAddress = readOptional(
            key = SMTP_FROM_ADDRESS,
            environment = environment,
            systemProperties = systemProperties,
            dotEnvValues = dotEnvValues,
            defaultValue = username,
        )

        val fromName = readOptional(
            key = SMTP_FROM_NAME,
            environment = environment,
            systemProperties = systemProperties,
            dotEnvValues = dotEnvValues,
            defaultValue = "VitaMo",
        )

        val sslEnabled = readOptional(
            key = SMTP_SSL_ENABLED,
            environment = environment,
            systemProperties = systemProperties,
            dotEnvValues = dotEnvValues,
            defaultValue = if (port == 465) "true" else "false",
        ).toBooleanStrictOrNull() ?: error("Invalid value for $SMTP_SSL_ENABLED")

        val startTlsEnabled = readOptional(
            key = SMTP_STARTTLS_ENABLED,
            environment = environment,
            systemProperties = systemProperties,
            dotEnvValues = dotEnvValues,
            defaultValue = if (port == 587) "true" else "false",
        ).toBooleanStrictOrNull() ?: error("Invalid value for $SMTP_STARTTLS_ENABLED")

        val authEnabled = readOptional(
            key = SMTP_AUTH_ENABLED,
            environment = environment,
            systemProperties = systemProperties,
            dotEnvValues = dotEnvValues,
            defaultValue = "true",
        ).toBooleanStrictOrNull() ?: error("Invalid value for $SMTP_AUTH_ENABLED")

        return SmtpConfig(
            host = host,
            port = port,
            username = username,
            password = password,
            fromAddress = fromAddress,
            fromName = fromName,
            sslEnabled = sslEnabled,
            startTlsEnabled = startTlsEnabled,
            authEnabled = authEnabled,
        )
    }

    private fun readRequired(
        key: String,
        environment: Map<String, String>,
        systemProperties: Properties,
        dotEnvValues: Map<String, String>,
    ): String {
        return readOptional(
            key = key,
            environment = environment,
            systemProperties = systemProperties,
            dotEnvValues = dotEnvValues,
            defaultValue = "",
        ).ifBlank {
            error("Missing required SMTP config value: $key")
        }
    }

    private fun readOptional(
        key: String,
        environment: Map<String, String>,
        systemProperties: Properties,
        dotEnvValues: Map<String, String>,
        defaultValue: String,
    ): String {
        return (environment[key]
            ?: systemProperties.getProperty(key)
            ?: dotEnvValues[key]
            ?: defaultValue
        ).trim().ifBlank { defaultValue }
    }

    private fun loadDotEnv(dotEnvPath: Path): Map<String, String> {
        if (!Files.exists(dotEnvPath)) {
            return emptyMap()
        }

        return Files.readAllLines(dotEnvPath)
            .asSequence()
            .map(String::trim)
            .filter { it.isNotBlank() && !it.startsWith("#") && it.contains("=") }
            .map { line ->
                val separatorIndex = line.indexOf('=')
                val key = line.substring(0, separatorIndex).trim()
                val value = line.substring(separatorIndex + 1).trim().trim('"', '\'')
                key to value
            }
            .filter { (key, _) -> key.isNotBlank() }
            .toMap()
    }
}
