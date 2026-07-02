package eu.vitamo.app.features.feed.routes

import eu.vitamo.app.api.contracts.feed.PrivacyStatus
import eu.vitamo.app.features.feed.FakeFeedRepository
import eu.vitamo.app.features.feed.FakeFriendshipService
import eu.vitamo.app.features.feed.FakeUserRepository
import eu.vitamo.app.features.feed.sampleCreateRequest
import eu.vitamo.app.features.feed.usecase.GetFeedPageUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
