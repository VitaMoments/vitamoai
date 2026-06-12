package eu.vitamo.app.database

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway

class FlywayMigrator(
    private val dataSource: HikariDataSource,
) {
    fun migrate() {
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }
}

