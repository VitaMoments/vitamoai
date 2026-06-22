package eu.vitamo.app.config

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
        valueReader: (String) -> String? = { key -> EnvLoader.read(key, environment, systemProperties) },
    ): SmtpConfig {
        val host = readRequired(SMTP_HOST, valueReader)
        val username = readRequired(SMTP_USERNAME, valueReader)
        val password = readRequired(SMTP_PASSWORD, valueReader)

        val port = readOptional(
            key = SMTP_PORT,
            valueReader = valueReader,
            defaultValue = "465",
        ).toIntOrNull() ?: error("Invalid value for $SMTP_PORT")

        val fromAddress = readOptional(
            key = SMTP_FROM_ADDRESS,
            valueReader = valueReader,
            defaultValue = username,
        )

        val fromName = readOptional(
            key = SMTP_FROM_NAME,
            valueReader = valueReader,
            defaultValue = "VitaMo",
        )

        val sslEnabled = readOptional(
            key = SMTP_SSL_ENABLED,
            valueReader = valueReader,
            defaultValue = if (port == 465) "true" else "false",
        ).toBooleanStrictOrNull() ?: error("Invalid value for $SMTP_SSL_ENABLED")

        val startTlsEnabled = readOptional(
            key = SMTP_STARTTLS_ENABLED,
            valueReader = valueReader,
            defaultValue = if (port == 587) "true" else "false",
        ).toBooleanStrictOrNull() ?: error("Invalid value for $SMTP_STARTTLS_ENABLED")

        val authEnabled = readOptional(
            key = SMTP_AUTH_ENABLED,
            valueReader = valueReader,
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
        valueReader: (String) -> String?,
    ): String {
        return readOptional(
            key = key,
            valueReader = valueReader,
            defaultValue = "",
        ).ifBlank {
            error("Missing required SMTP config value: $key")
        }
    }

    private fun readOptional(
        key: String,
        valueReader: (String) -> String?,
        defaultValue: String,
    ): String {
        return valueReader(key)?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: defaultValue
    }
}
