package eu.vitamo.app.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.koin.dsl.module

val databaseModule = module {
    single { DatabaseConfig.fromEnvironment() }

    single {
        val config = get<DatabaseConfig>()

        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = config.url
                driverClassName = config.driver
                username = config.username
                password = config.password
                maximumPoolSize = config.maximumPoolSize
            },
        )
    }

    single { FlywayMigrator(get()) }
    single { DatabaseFactory(get(), get()) }
}

