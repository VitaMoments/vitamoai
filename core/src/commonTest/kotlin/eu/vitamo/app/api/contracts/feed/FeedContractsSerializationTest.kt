package eu.vitamo.app.api.contracts.feed

import eu.vitamo.app.api.contracts.user.PublicUser
import eu.vitamo.app.api.contracts.user.UserRole
import eu.vitamo.app.serialization.AppJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FeedContractsSerializationTest {
    @Test
    fun `feed item serializes and deserializes with canonical model`() {
        val item = FeedItem(
            uuid = Uuid.parse("123e4567-e89b-12d3-a456-426614174000"),
            author = PublicUser(
                id = Uuid.parse("123e4567-e89b-12d3-a456-426614174001"),
                displayName = "Ava",
                bio = null,
                role = UserRole.USER,
            ),
            content = RichTextDocument(
                type = "markdown",
                content = AppJson.parseToJsonElement("""{"text":"hello"}"""),
            ),
            privacy = PrivacyStatus.PUBLIC,
            categories = listOf(FeedCategory.MENTAL),
            mediaAssets = emptyList(),
            createdAt = Instant.parse("2026-03-19T10:30:00Z"),
            updatedAt = Instant.parse("2026-03-19T10:30:00Z"),
            deletedAt = null,
        )

        val encoded = AppJson.encodeToString(FeedItem.serializer(), item)
        val decoded = AppJson.decodeFromString(FeedItem.serializer(), encoded)

        assertEquals(item, decoded)
    }
}
