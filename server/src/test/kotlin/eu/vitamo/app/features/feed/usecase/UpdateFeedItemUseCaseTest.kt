package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.common.PrivacyStatus
import eu.vitamo.app.api.contracts.common.RichTextDocument
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.features.feed.FakeFeedRepository
import eu.vitamo.app.features.feed.FakeUserRepository
import eu.vitamo.app.features.feed.model.FeedException
import eu.vitamo.app.features.feed.sampleCreateRequest
import eu.vitamo.app.features.feed.validation.FeedInputValidator
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

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

        assertEquals("new", updated.content?.content?.jsonObject?.get("text")?.toString()?.trim('"'))
    }

    @Test
    fun `privacy change on update is persisted`() = runTest {
        val repo = FakeFeedRepository()
        val users = FakeUserRepository()
        val created = repo.create(users.user.id, sampleCreateRequest("test", PrivacyStatus.PUBLIC), kotlin.time.Clock.System.now())
        val useCase = UpdateFeedItemUseCase(repo, users, FeedInputValidator())

        val updated = useCase(
            users.user.id,
            created.uuid,
            UpdateFeedItemRequest(privacy = PrivacyStatus.FRIENDS_ONLY),
        )

        assertEquals(PrivacyStatus.FRIENDS_ONLY, updated.privacy)
        assertTrue(updated.updatedAt >= created.updatedAt)
    }

    @Test
    fun `invalid content on update throws InvalidContent`() = runTest {
        val repo = FakeFeedRepository()
        val users = FakeUserRepository()
        val created = repo.create(users.user.id, sampleCreateRequest("original"), kotlin.time.Clock.System.now())
        val useCase = UpdateFeedItemUseCase(repo, users, FeedInputValidator())

        assertFailsWith<FeedException.InvalidContent> {
            useCase(
                users.user.id,
                created.uuid,
                UpdateFeedItemRequest(
                    content = RichTextDocument(
                        type = "markdown",
                        content = buildJsonObject {},
                    ),
                ),
            )
        }
    }
}
