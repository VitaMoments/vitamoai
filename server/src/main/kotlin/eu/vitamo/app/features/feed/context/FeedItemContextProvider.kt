package eu.vitamo.app.features.feed.context

import eu.vitamo.app.api.contracts.feed.FeedItemInteractionContext
import eu.vitamo.app.features.feed.model.FeedItemRecord
import kotlin.uuid.Uuid

interface FeedItemContextProvider {

    suspend fun resolve(
        currentUserId: Uuid,
        feedItem: FeedItemRecord,
    ): FeedItemInteractionContext

    suspend fun resolveAll(
        currentUserId: Uuid,
        feedItems: List<FeedItemRecord>,
    ): Map<Uuid, FeedItemInteractionContext>
}