package eu.vitamo.app.features.feed.mapper

import eu.vitamo.app.features.feed.model.FeedItemReactionRecord
import eu.vitamo.app.features.feed.model.PostRecord
import eu.vitamo.app.features.feed.table.FeedItemReactionTable
import eu.vitamo.app.features.feed.table.FeedItemTable
import eu.vitamo.app.features.feed.table.PostTable
import org.jetbrains.exposed.v1.core.ResultRow
import kotlin.time.Instant

fun ResultRow.toReactionRecord() =
    FeedItemReactionRecord(
        id = this[FeedItemReactionTable.id].value,
        feedItemId = this[FeedItemReactionTable.feedItemId].value,
        parentReactionId = this[FeedItemReactionTable.parentReactionId],
        authorId = this[FeedItemReactionTable.authorId].value,
        contentJson = this[FeedItemReactionTable.contentJson],
        createdAt = Instant.fromEpochSeconds(this[FeedItemReactionTable.createdAt],
        ),
        updatedAt = Instant.fromEpochSeconds(this[FeedItemReactionTable.updatedAt],
        ),
        deletedAt = this[FeedItemReactionTable.deletedAt]?.let(Instant::fromEpochSeconds),
    )

fun ResultRow.toPostRecord() =
    PostRecord(
        id = this[FeedItemTable.id].value,
        authorId = this[FeedItemTable.authorId].value,
        title = this[PostTable.title],
        messageJson = this[PostTable.messageJson],
        createdAt = Instant.fromEpochSeconds(this[FeedItemTable.createdAt],),
        updatedAt = Instant.fromEpochSeconds(this[FeedItemTable.updatedAt],),
        deletedAt = this[FeedItemTable.deletedAt]?.let(Instant::fromEpochSeconds,),
    )