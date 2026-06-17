package eu.vitamo.app.auth.config

import java.nio.file.Files
import java.nio.file.Path
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
        dotEnvPath: Path = Path.of(".env"),
    ): JWTConfig {
        val dotEnvValues = loadDotEnv(dotEnvPath)

        val issuer = readRequired(JWT_ISSUER, environment, systemProperties, dotEnvValues)
        val audience = readRequired(JWT_AUDIENCE, environment, systemProperties, dotEnvValues)
        val secret = readRequired(JWT_SECRET, environment, systemProperties, dotEnvValues)

        val realm = readOptional(
            key = JWT_REALM,
            environment = environment,
            systemProperties = systemProperties,
            dotEnvValues = dotEnvValues,
            defaultValue = "access",
        )

        val accessExpiration = readOptional(
            key = JWT_ACCESS_EXP_SECONDS,
            environment = environment,
            systemProperties = systemProperties,
            dotEnvValues = dotEnvValues,
            defaultValue = readOptional(
                key = JWT_EXP_SECONDS,
                environment = environment,
                systemProperties = systemProperties,
                dotEnvValues = dotEnvValues,
                defaultValue = "3600",
            ),
        ).toLongOrNull() ?: error("Invalid value for $JWT_ACCESS_EXP_SECONDS")

        val refreshExpiration = readOptional(
            key = JWT_REFRESH_EXP_SECONDS,
            environment = environment,
            systemProperties = systemProperties,
            dotEnvValues = dotEnvValues,
            defaultValue = "604800",
        ).toLongOrNull() ?: error("Invalid value for $JWT_REFRESH_EXP_SECONDS")

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
        dotEnvValues: Map<String, String>,
    ): String {
        return readOptional(
            key = key,
            environment = environment,
            systemProperties = systemProperties,
            dotEnvValues = dotEnvValues,
            defaultValue = "",
        ).ifBlank {
            error("Missing required JWT config value: $key")
        }
    }

    private fun readOptional(
        key: String,
        environment: Map<String, String>,
        systemProperties: Properties,
        dotEnvValues: Map<String, String>,
        defaultValue: String,
    ): String {
        return environment[key]
            ?: systemProperties.getProperty(key)
            ?: dotEnvValues[key]
            ?: defaultValue
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
