package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.feed.RichTextDocument
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.features.feed.FakeFeedRepository
import eu.vitamo.app.features.feed.FakeUserRepository
import eu.vitamo.app.features.feed.sampleCreateRequest
import eu.vitamo.app.features.feed.validation.FeedInputValidator
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateFeedItemUseCaseTest {
    @Test
    fun `updates own feed item`() = runTest {
        val repo = FakeFeedRepository()
        val users = FakeUserRepository()
        val created = repo.create(users.user.id, sampleCreateRequest("old"), kotlin.time.Clock.System.now())
        val useCase = UpdateFeedItemUseCase(repo, users, FeedInputValidator())

        val updated = useCase(
            users.user.id,
            created.uuid,
            UpdateFeedItemRequest(
                content = RichTextDocument(
                    type = "markdown",
                    content = buildJsonObject { put("text", "new") },
                ),
            ),
        )

        assertEquals("new", updated.content.content.jsonObject["text"]?.toString()?.trim('"'))
    }
}
