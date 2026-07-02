package eu.vitamo.app.features.feed.routes

import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.features.feed.FakeFeedRepository
import eu.vitamo.app.features.feed.FakeUserRepository
import eu.vitamo.app.features.feed.sampleCreateRequest
import eu.vitamo.app.features.feed.usecase.CreateFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.DeleteFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.GetFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.UpdateFeedItemUseCase
import eu.vitamo.app.features.feed.validation.FeedInputValidator
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FeedFlowIntegrationTest {
    @Test
    fun `create update delete lifecycle works through use-cases`() = runTest {
        val repo = FakeFeedRepository()
        val users = FakeUserRepository()
        val validator = FeedInputValidator()

        val create = CreateFeedItemUseCase(repo, users, validator)
        val update = UpdateFeedItemUseCase(repo, users, validator)
        val read = GetFeedItemUseCase(repo, users, eu.vitamo.app.features.feed.FakeFriendshipService())
        val delete = DeleteFeedItemUseCase(repo)

        val item = create(users.user.id, sampleCreateRequest("first"))
        assertNotNull(read(users.user.id, item.uuid))

        val updated = update(users.user.id, item.uuid, UpdateFeedItemRequest())
        assertNotNull(updated)

        delete(users.user.id, item.uuid)
        assertTrue(repo.items.any { it.uuid == item.uuid && it.deletedAt != null })
    }
}
