package eu.vitamo.app.database

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.slf4j.LoggerFactory

class DatabaseFactory(
    private val dataSource: HikariDataSource,
    private val flywayMigrator: FlywayMigrator,
) {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun init() {
        flywayMigrator.migrate()
        Database.connect(dataSource)

        logger.info(
            "Database initialized. url={}, maxPoolSize={}",
            sanitizeJdbcUrl(dataSource.jdbcUrl),
            dataSource.maximumPoolSize,
        )
    }

    private fun sanitizeJdbcUrl(url: String): String {
        return url
            .replace(Regex("(?i)(password=)[^&;]+"), "$1****")
            .replace(Regex("(?i)(user=)[^&;]+"), "$1****")
    }
}


