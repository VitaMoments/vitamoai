package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.common.RichTextDocument
import eu.vitamo.app.api.contracts.feed.FeedItemComment
import eu.vitamo.app.features.feed.context.FeedItemReactionContextLoader
import eu.vitamo.app.features.feed.repository.FeedItemReactionRepository
import eu.vitamo.app.features.feed.repository.FeedItemRepository
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.uuid.Uuid

class CreateFeedItemCommentUseCase(
    private val feedItemRepository: FeedItemRepository,
    private val reactionRepository: FeedItemReactionRepository,
    private val reactionContextLoader:
        FeedItemReactionContextLoader,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        feedItemId: Uuid,
        content: RichTextDocument,
    ): RepositoryResult<FeedItemComment> {

        /*
         * Eerst controleren of het FeedItem bestaat
         * en niet soft-deleted is.
         */
        when (
            val feedItemResult =
                feedItemRepository.findById(
                    feedItemId = feedItemId,
                )
        ) {
            is RepositoryResult.Success -> Unit

            is RepositoryResult.Error -> {
                return RepositoryResult.Error(
                    error = feedItemResult.error,
                )
            }
        }

        val contentJson =
            Json.encodeToString(
                value = content,
            )

        return when (
            val result =
                reactionRepository.createComment(
                    feedItemId = feedItemId,
                    authorId = currentUserId,
                    contentJson = contentJson,
                    createdAt = Clock.System.now(),
                )
        ) {
            is RepositoryResult.Success -> {
                val comment =
                    reactionContextLoader.load(
                        currentUserId =
                            currentUserId,
                        reaction = result.data,
                    ) as? FeedItemComment
                        ?: return RepositoryResult.Error(
                            error =
                                RepositoryError.Internal(
                                    message =
                                        "Created comment could not be loaded",
                                ),
                        )

                RepositoryResult.Success(
                    data = comment,
                )
            }

            is RepositoryResult.Error -> {
                RepositoryResult.Error(
                    error = result.error,
                )
            }
        }
    }
}