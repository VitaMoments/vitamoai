package eu.vitamo.app.features.feed.routes

import eu.vitamo.app.api.contracts.common.PrivacyStatus
import eu.vitamo.app.features.feed.FakeFeedRepository
import eu.vitamo.app.features.feed.FakeFriendshipService
import eu.vitamo.app.features.feed.FakeUserRepository
import eu.vitamo.app.features.feed.sampleCreateRequest
import eu.vitamo.app.features.feed.model.FeedException
import eu.vitamo.app.features.feed.usecase.GetFeedItemUseCase
import eu.vitamo.app.features.feed.usecase.GetFeedPageUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FeedReadRouteTest {
    @Test
    fun `general feed returns public items`() = runTest {
        val repo = FakeFeedRepository()
        val users = FakeUserRepository()
        repo.create(users.user.id, sampleCreateRequest("public", PrivacyStatus.PUBLIC), kotlin.time.Clock.System.now())

        val useCase = GetFeedPageUseCase(repo, users, FakeFriendshipService())
        val page = useCase.generalFeed(users.user.id, limit = 20, offset = 0, categories = emptySet())

        assertEquals(1, page.items.size)
    }

    @Test
    fun `friends-only item is hidden from non-friends`() = runTest {
        val repo = FakeFeedRepository()
        val users = FakeUserRepository()
        val owner = users.user.id

        // Owner creates a FRIENDS_ONLY item
        val created = repo.create(owner, sampleCreateRequest("secret", PrivacyStatus.FRIENDS_ONLY), kotlin.time.Clock.System.now())

        // Non-friend tries to read it via GetFeedItemUseCase
        val nonFriendId = kotlin.uuid.Uuid.parse("123e4567-e89b-12d3-a456-426614174099")
        val getItemUseCase = GetFeedItemUseCase(repo, users, FakeFriendshipService(areFriends = false))

        assertFailsWith<FeedException.Forbidden> {
            getItemUseCase(nonFriendId, created.uuid)
        }
    }

    @Test
    fun `friends-only item is visible to friends`() = runTest {
        val repo = FakeFeedRepository()
        val users = FakeUserRepository()
        val owner = users.user.id

        val created = repo.create(owner, sampleCreateRequest("secret", PrivacyStatus.FRIENDS_ONLY), kotlin.time.Clock.System.now())

        val friendId = kotlin.uuid.Uuid.parse("123e4567-e89b-12d3-a456-426614174099")
        val getItemUseCase = GetFeedItemUseCase(repo, users, FakeFriendshipService(areFriends = true))

        val item = getItemUseCase(friendId, created.uuid)
        assertEquals(created.uuid, item.uuid)
    }
}
