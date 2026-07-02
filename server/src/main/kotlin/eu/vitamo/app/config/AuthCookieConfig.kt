package eu.vitamo.app.config

import java.util.Properties

data class AuthCookieConfig(
    val secure: Boolean,
) {
    companion object {
        fun fromEnvironment(
            environment: Map<String, String> = System.getenv(),
            systemProperties: Properties = System.getProperties(),
        ): AuthCookieConfig {
            val explicitSecure = EnvLoader.read(
                key = "AUTH_COOKIE_SECURE",
                environment = environment,
                systemProperties = systemProperties,
            )?.toBooleanStrictOrNull()

            if (explicitSecure != null) {
                return AuthCookieConfig(secure = explicitSecure)
            }

            return AuthCookieConfig(
                secure = AppEnvironmentLoader.load(environment, systemProperties) == AppEnvironment.PRODUCTION,
            )
        }
    }
}
