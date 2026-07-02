package eu.vitamo.app.features.feed.routes

import eu.vitamo.app.features.feed.FakeFeedRepository
import eu.vitamo.app.features.feed.FakeUserRepository
import eu.vitamo.app.features.feed.model.FeedException
import eu.vitamo.app.features.feed.sampleCreateRequest
import eu.vitamo.app.features.feed.usecase.DeleteFeedItemUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class FeedDeleteRouteTest {
    @Test
    fun `owner can soft delete and non-owner cannot`() = runTest {
        val repo = FakeFeedRepository()
        val users = FakeUserRepository()
        val item = repo.create(users.user.id, sampleCreateRequest(), kotlin.time.Clock.System.now())
        val useCase = DeleteFeedItemUseCase(repo)

        useCase(users.user.id, item.uuid)
        assertTrue(repo.items.any { it.uuid == item.uuid && it.deletedAt != null })

        assertFailsWith<FeedException.Forbidden> {
            useCase(Uuid.random(), item.uuid)
        }
    }
}
