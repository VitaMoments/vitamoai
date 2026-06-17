package eu.vitamo.app.database

import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseConfigTest {

    @Test
    fun fromEnvironment_usesDefaultsWhenVariablesAreMissing() {
        val config = DatabaseConfig.fromEnvironment(environment = emptyMap())

        assertEquals("jdbc:postgresql://localhost:5432/vitamoai", config.url)
        assertEquals("org.postgresql.Driver", config.driver)
        assertEquals("postgres", config.username)
        assertEquals("postgres", config.password)
        assertEquals(3, config.maximumPoolSize)
    }

    @Test
    fun fromEnvironment_usesProvidedValues() {
        val config = DatabaseConfig.fromEnvironment(
            environment = mapOf(
                "DATABASE_HOST" to "localhost",
                "DATABASE_PORT" to "5432",
                "DATABASE_NAME" to "custom",
                "DATABASE_USER" to "dbuser",
                "DATABASE_PASSWORD" to "dbpass",
                "DATABASE_MAX_POOL_SIZE" to "24",
            ),
        )

        assertEquals("jdbc:postgresql://localhost:5432/custom", config.url)
        assertEquals("org.postgresql.Driver", config.driver)
        assertEquals("dbuser", config.username)
        assertEquals("dbpass", config.password)
        assertEquals(24, config.maximumPoolSize)
    }

    @Test
    fun fromEnvironment_fallsBackToDefaultWhenPoolSizeIsInvalid() {
        val config = DatabaseConfig.fromEnvironment(
            environment = mapOf("DATABASE_MAX_POOL_SIZE" to "not-a-number"),
        )

        assertEquals(3, config.maximumPoolSize)
    }
}
