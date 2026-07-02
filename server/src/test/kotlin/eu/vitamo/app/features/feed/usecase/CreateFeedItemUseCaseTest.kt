package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.features.feed.FakeFeedRepository
import eu.vitamo.app.features.feed.FakeUserRepository
import eu.vitamo.app.features.feed.sampleCreateRequest
import eu.vitamo.app.features.feed.validation.FeedInputValidator
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateFeedItemUseCaseTest {
    @Test
    fun `creates feed item for authenticated user`() = runTest {
        val repo = FakeFeedRepository()
        val users = FakeUserRepository()
        val useCase = CreateFeedItemUseCase(repo, users, FeedInputValidator())

        val result = useCase(users.user.id, sampleCreateRequest())

        assertEquals(users.user.id, result.author.id)
        assertEquals(1, repo.items.size)
    }
}
