package eu.vitamo.app.database

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.slf4j.LoggerFactory

class DatabaseFactory(
    private val dataSource: HikariDataSource,
    private val flywayMigrator: FlywayMigrator,
    private val environment: Map<String, String> = System.getenv(),
) {
    private val logger =
        LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun init() {
        val appEnvironment = environment[APP_ENV]
            ?.trim()
            ?.lowercase()
            ?: DEFAULT_APP_ENV

        val cleanOnStart = environment[FLYWAY_CLEAN_ON_START]
            ?.trim()
            ?.toBooleanStrictOrNull()
            ?: false

        if (cleanOnStart) {
            require(appEnvironment == LOCAL_ENVIRONMENT) {
                "$FLYWAY_CLEAN_ON_START may only be enabled when " +
                        "$APP_ENV=$LOCAL_ENVIRONMENT."
            }

            flywayMigrator.cleanAndMigrate(
                allowClean = true,
            )
        } else {
            flywayMigrator.migrate()
        }

        Database.connect(dataSource)

        logger.info(
            "Database initialized. url={}, maxPoolSize={}, environment={}",
            sanitizeJdbcUrl(dataSource.jdbcUrl),
            dataSource.maximumPoolSize,
            appEnvironment,
        )
    }

    private fun sanitizeJdbcUrl(
        url: String,
    ): String {
        return url
            .replace(
                Regex("(?i)(password=)[^&;]+"),
                "$1****",
            )
            .replace(
                Regex("(?i)(user=)[^&;]+"),
                "$1****",
            )
    }

    private companion object {
        const val APP_ENV =
            "APP_ENV"

        const val FLYWAY_CLEAN_ON_START =
            "FLYWAY_CLEAN_ON_START"

        const val LOCAL_ENVIRONMENT =
            "local"

        const val DEFAULT_APP_ENV =
            "development"
    }
}