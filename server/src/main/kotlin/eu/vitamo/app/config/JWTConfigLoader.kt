package eu.vitamo.app.config

import java.util.Properties

object JWTConfigLoader {
    private const val JWT_ISSUER = "JWT_ISSUER"
    private const val JWT_AUDIENCE = "JWT_AUDIENCE"
    private const val JWT_SECRET = "JWT_SECRET"
    private const val JWT_REALM = "JWT_REALM"
    private const val JWT_ACCESS_EXP_SECONDS = "JWT_ACCESS_EXP_SECONDS"
    private const val JWT_REFRESH_EXP_SECONDS = "JWT_REFRESH_EXP_SECONDS"
    private const val JWT_EXP_SECONDS = "JWT_EXP_SECONDS"

    fun loadOrThrow(
        environment: Map<String, String> = System.getenv(),
        systemProperties: Properties = System.getProperties(),
    ): JWTConfig {
        val issuer = readRequired(
            key = JWT_ISSUER,
            environment = environment,
            systemProperties = systemProperties,
        )

        val audience = readRequired(
            key = JWT_AUDIENCE,
            environment = environment,
            systemProperties = systemProperties,
        )

        val secret = readRequired(
            key = JWT_SECRET,
            environment = environment,
            systemProperties = systemProperties,
        )

        val realm = readOptional(
            key = JWT_REALM,
            environment = environment,
            systemProperties = systemProperties,
            defaultValue = "access",
        )

        val accessExpirationRaw = readOptional(
            key = JWT_ACCESS_EXP_SECONDS,
            environment = environment,
            systemProperties = systemProperties,
            defaultValue = readOptional(
                key = JWT_EXP_SECONDS,
                environment = environment,
                systemProperties = systemProperties,
                defaultValue = "3600",
            ),
        )

        val accessExpiration = accessExpirationRaw.toLongOrNull()
            ?: error("Invalid value for $JWT_ACCESS_EXP_SECONDS")

        val refreshExpirationRaw = readOptional(
            key = JWT_REFRESH_EXP_SECONDS,
            environment = environment,
            systemProperties = systemProperties,
            defaultValue = "604800",
        )

        val refreshExpiration = refreshExpirationRaw.toLongOrNull()
            ?: error("Invalid value for $JWT_REFRESH_EXP_SECONDS")

        return JWTConfig(
            issuer = issuer,
            audience = audience,
            realm = realm,
            secret = secret,
            accessTokenExpirationSeconds = accessExpiration,
            refreshTokenExpirationSeconds = refreshExpiration,
        )
    }

    private fun readRequired(
        key: String,
        environment: Map<String, String>,
        systemProperties: Properties,
    ): String {
        return readOptional(
            key = key,
            environment = environment,
            systemProperties = systemProperties,
            defaultValue = "",
        ).ifBlank {
            error("Missing required JWT config value: $key")
        }
    }

    private fun readOptional(
        key: String,
        environment: Map<String, String>,
        systemProperties: Properties,
        defaultValue: String,
    ): String {
        return EnvLoader.read(
            key = key,
            environment = environment,
            systemProperties = systemProperties,
        )
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: defaultValue
    }
}