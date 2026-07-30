package eu.vitamo.app.database

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory

class FlywayMigrator(
    private val dataSource: HikariDataSource,
) {
    private val logger =
        LoggerFactory.getLogger(FlywayMigrator::class.java)

    fun migrate() {
        val result = createFlyway()
            .migrate()

        logger.info(
            "Flyway migration completed. migrationsExecuted={}",
            result.migrationsExecuted,
        )
    }

    /**
     * Verwijdert alle databaseobjecten uit de door Flyway beheerde schema's
     * en voert daarna alle migraties opnieuw uit.
     *
     * Alleen gebruiken voor lokale ontwikkeling en geautomatiseerde tests.
     */
    fun cleanAndMigrate(
        allowClean: Boolean,
    ) {
        require(allowClean) {
            "Flyway clean is disabled. Enable it explicitly for local development."
        }

        val flyway = createFlyway(
            cleanDisabled = false,
        )

        logger.warn(
            "Cleaning the complete Flyway database schema. All local data will be deleted.",
        )

        flyway.clean()

        val result = flyway.migrate()

        logger.info(
            "Flyway clean and migration completed. migrationsExecuted={}",
            result.migrationsExecuted,
        )
    }

    private fun createFlyway(
        cleanDisabled: Boolean = true,
    ): Flyway {
        return Flyway.configure()
            .dataSource(dataSource)
            .locations(MIGRATION_LOCATION)
            .cleanDisabled(cleanDisabled)
            .load()
    }

    private companion object {
        const val MIGRATION_LOCATION =
            "classpath:db/migration"
    }
}