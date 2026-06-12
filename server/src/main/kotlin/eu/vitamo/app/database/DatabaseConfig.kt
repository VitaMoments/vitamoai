package eu.vitamo.app.database

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val databaseName: String,
    val driver: String,
    val username: String,
    val password: String,
    val maximumPoolSize: Int,
) {
    val url: String
        get() = "jdbc:postgresql://$host:$port/$databaseName"

    companion object {
        private const val DATABASE_HOST = "DATABASE_HOST"
        private const val DATABASE_PORT = "DATABASE_PORT"
        private const val DATABASE_NAME = "DATABASE_NAME"
        private const val DATABASE_USER = "DATABASE_USER"
        private const val DATABASE_PASSWORD = "DATABASE_PASSWORD"
        private const val DATABASE_MAX_POOL_SIZE = "DATABASE_MAX_POOL_SIZE"

        private const val DEFAULT_HOST = "localhost"
        private const val DEFAULT_PORT = 5432
        private const val DEFAULT_DATABASE_NAME = "vitamoai"
        private const val DEFAULT_USER = "postgres"
        private const val DEFAULT_PASSWORD = "postgres"
        private const val DEFAULT_MAX_POOL_SIZE = 3
        private const val POSTGRES_DRIVER = "org.postgresql.Driver"

        fun fromEnvironment(environment: Map<String, String> = System.getenv()): DatabaseConfig {
            val parsedPort = environment[DATABASE_PORT]
                ?.toIntOrNull()
                ?.takeIf { it > 0 }
                ?: DEFAULT_PORT

            val parsedPoolSize = environment[DATABASE_MAX_POOL_SIZE]
                ?.toIntOrNull()
                ?.takeIf { it > 0 }
                ?: DEFAULT_MAX_POOL_SIZE

            return DatabaseConfig(
                host = environment[DATABASE_HOST] ?: DEFAULT_HOST,
                port = parsedPort,
                databaseName = environment[DATABASE_NAME] ?: DEFAULT_DATABASE_NAME,
                driver = POSTGRES_DRIVER,
                username = environment[DATABASE_USER] ?: DEFAULT_USER,
                password = environment[DATABASE_PASSWORD] ?: DEFAULT_PASSWORD,
                maximumPoolSize = parsedPoolSize,
            )
        }
    }
}

