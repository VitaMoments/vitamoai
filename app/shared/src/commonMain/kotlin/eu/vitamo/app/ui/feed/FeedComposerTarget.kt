package eu.vitamo.app.ui.feed

import kotlin.uuid.Uuid

sealed interface FeedComposerTarget {

    val feedItemId: Uuid

    data class Comment(
        override val feedItemId: Uuid,
    ) : FeedComposerTarget

    data class Reply(
        override val feedItemId: Uuid,
        val commentId: Uuid,
    ) : FeedComposerTarget
}