package eu.vitamo.app.features.feed.context

import eu.vitamo.app.api.contracts.feed.FeedItemInteractionContext
import eu.vitamo.app.features.feed.model.FeedItemReactionRecord
import kotlin.uuid.Uuid

interface FeedItemReactionContextProvider {

    suspend fun resolve(
        currentUserId: Uuid,
        reaction: FeedItemReactionRecord,
    ): FeedItemInteractionContext

    suspend fun resolveAll(
        currentUserId: Uuid,
        reactions: List<FeedItemReactionRecord>,
    ): Map<Uuid, FeedItemInteractionContext>
}