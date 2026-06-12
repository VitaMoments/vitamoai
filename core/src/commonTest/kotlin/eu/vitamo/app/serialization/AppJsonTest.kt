package eu.vitamo.app.serialization

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AppJsonTest {
    @Serializable
    private data class SamplePayload(
        @Serializable(with = UuidSerializer::class)
        val id: Uuid,
        @Serializable(with = InstantSerializer::class)
        val createdAt: Instant,
    )

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    @Test
    fun encodesAndDecodesUuidAndInstantAsStrings() {
        val payload = SamplePayload(
            id = Uuid.parse("123e4567-e89b-12d3-a456-426614174000"),
            createdAt = Instant.parse("2026-06-12T12:00:00Z"),
        )

        val encoded = AppJson.encodeToString(SamplePayload.serializer(), payload)
        val decoded = AppJson.decodeFromString(SamplePayload.serializer(), encoded)

        assertEquals(
            "{\"id\":\"123e4567-e89b-12d3-a456-426614174000\",\"createdAt\":\"2026-06-12T12:00:00Z\"}",
            encoded,
        )
        assertEquals(payload, decoded)
    }
}

