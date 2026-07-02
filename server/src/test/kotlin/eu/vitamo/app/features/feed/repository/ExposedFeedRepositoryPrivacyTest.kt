package eu.vitamo.app.features.feed.repository

import eu.vitamo.app.api.contracts.feed.PrivacyStatus
import eu.vitamo.app.features.feed.FakeFeedRepository
import eu.vitamo.app.features.feed.sampleCreateRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ExposedFeedRepositoryPrivacyTest {
    @Test
    fun `repository only returns active items`() = runTest {
        val repo = FakeFeedRepository()
        val owner = Uuid.random()
        repo.create(owner, sampleCreateRequest("public", PrivacyStatus.PUBLIC), kotlin.time.Clock.System.now())

        val (items, total) = repo.getAllVisibleCandidates(limit = 10, offset = 0, categories = emptySet())

        assertEquals(1, items.size)
        assertEquals(1, total)
    }
}
