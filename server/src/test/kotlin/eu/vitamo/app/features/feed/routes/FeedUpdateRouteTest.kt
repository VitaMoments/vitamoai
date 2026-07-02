package eu.vitamo.app.features.feed.routes

import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.features.feed.FakeFeedRepository
import eu.vitamo.app.features.feed.FakeUserRepository
import eu.vitamo.app.features.feed.model.FeedException
import eu.vitamo.app.features.feed.sampleCreateRequest
import eu.vitamo.app.features.feed.usecase.UpdateFeedItemUseCase
import eu.vitamo.app.features.feed.validation.FeedInputValidator
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.uuid.Uuid

class FeedUpdateRouteTest {
    @Test
    fun `non-owner update is forbidden`() = runTest {
        val repo = FakeFeedRepository()
        val users = FakeUserRepository()
        val item = repo.create(users.user.id, sampleCreateRequest(), kotlin.time.Clock.System.now())
        val useCase = UpdateFeedItemUseCase(repo, users, FeedInputValidator())

        assertFailsWith<FeedException.Forbidden> {
            useCase(Uuid.random(), item.uuid, UpdateFeedItemRequest())
        }
    }
}
