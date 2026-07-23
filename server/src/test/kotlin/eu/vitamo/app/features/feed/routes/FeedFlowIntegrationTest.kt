package eu.vitamo.app.features.feed.routes

import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.features.feed.FakeFeedRepository
import eu.vitamo.app.features.feed.FakeFriendshipService
import eu.vitamo.app.features.feed.FakeUserRepository
import eu.vitamo.app.features.feed.sampleCreateRequest
import eu.vitamo.app.features.feed.usecase.CreateFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.DeleteFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.GetFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.GetFeedPageUseCase
import eu.vitamo.app.features.feed.usecase.UpdateFeedItemUseCase
import eu.vitamo.app.features.feed.validation.FeedInputValidator
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun `pagination boundary returns correct item counts and hasMore`() = runTest {
        val repo = FakeFeedRepository()
        val users = FakeUserRepository()
        val validator = FeedInputValidator()
        val create = CreateFeedItemUseCase(repo, users, validator)
        val pageUseCase = GetFeedPageUseCase(repo, users, FakeFriendshipService())

        // Create 25 items
        for (i in 1..25) {
            create(users.user.id, sampleCreateRequest("item $i"))
        }

        // Page 1: limit=10, offset=0
        val page1 = pageUseCase.myFeed(users.user.id, limit = 10, offset = 0)
        assertEquals(10, page1.items.size)
        assertEquals(25L, page1.total)
        assertTrue(page1.hasMore)

        // Page 3: limit=10, offset=20
        val page3 = pageUseCase.myFeed(users.user.id, limit = 10, offset = 20)
        assertEquals(5, page3.items.size)
        assertEquals(25L, page3.total)
        assertFalse(page3.hasMore)
    }
}
