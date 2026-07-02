package eu.vitamo.app.features.feed.repository

import eu.vitamo.app.features.feed.FakeFeedRepository
import eu.vitamo.app.features.feed.sampleCreateRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.uuid.Uuid

class ExposedFeedRepositorySoftDeleteTest {
    @Test
    fun `soft deleted items are excluded from active queries`() = runTest {
        val repo = FakeFeedRepository()
        val owner = Uuid.random()
        val created = repo.create(owner, sampleCreateRequest(), Clock.System.now())
        repo.softDelete(created.uuid, owner, Clock.System.now())

        val (items, total) = repo.getAllVisibleCandidates(limit = 10, offset = 0, categories = emptySet())
        assertTrue(items.isEmpty())
        assertEquals(0, total)
    }
}
