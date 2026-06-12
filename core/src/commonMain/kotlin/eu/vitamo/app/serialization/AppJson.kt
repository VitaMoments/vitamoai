package eu.vitamo.app.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Shared JSON serialization configuration for the app.
 * Used consistently across the server and client applications.
 */
val AppJson = Json {
    classDiscriminator = "type"
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    serializersModule = SerializersModule {
        contextual(Uuid::class, UuidSerializer)
        contextual(Instant::class, InstantSerializer)
    }
}



