package eu.vitamo.app.config

import java.util.Properties

enum class AppEnvironment {
    DEVELOPMENT,
    PRODUCTION,
}

object AppEnvironmentLoader {
    private const val VITAMO_ENV = "VITAMO_ENV"
    private const val APP_ENV = "APP_ENV"
    private const val KTOR_ENV = "KTOR_ENV"

    fun load(
        environment: Map<String, String> = System.getenv(),
        systemProperties: Properties = System.getProperties(),
    ): AppEnvironment {
        val rawValue = listOf(VITAMO_ENV, APP_ENV, KTOR_ENV)
            .firstNotNullOfOrNull { key ->
                EnvLoader.read(key = key, environment = environment, systemProperties = systemProperties)
            }
            .orEmpty()

        return when (rawValue.trim().lowercase()) {
            "production", "prod" -> AppEnvironment.PRODUCTION
            else -> AppEnvironment.DEVELOPMENT
        }
    }
}
