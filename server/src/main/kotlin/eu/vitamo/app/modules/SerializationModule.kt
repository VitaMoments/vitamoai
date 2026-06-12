package eu.vitamo.app.modules

import eu.vitamo.app.serialization.AppJson
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("SerializationModule")

/**
 * Configures JSON serialization for the server.
 *
 * - Sets up Ktor ContentNegotiation with AppJson
 * - Uses the shared AppJson configuration from core module
 */
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(AppJson)
    }

    logger.info("Serialization configured with AppJson")
}






