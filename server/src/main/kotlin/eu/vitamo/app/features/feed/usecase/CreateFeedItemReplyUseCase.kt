package eu.vitamo.app.features.feed.usecase

import eu.vitamo.app.api.contracts.common.RichTextDocument
import eu.vitamo.app.api.contracts.feed.FeedItemReply
import eu.vitamo.app.features.feed.context.FeedItemReactionContextLoader
import eu.vitamo.app.features.feed.repository.FeedItemReactionRepository
import eu.vitamo.app.features.feed.repository.FeedItemRepository
import eu.vitamo.app.repository.FieldError
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.uuid.Uuid

class CreateFeedItemReplyUseCase(
    private val feedItemRepository: FeedItemRepository,
    private val reactionRepository: FeedItemReactionRepository,
    private val reactionContextLoader:
        FeedItemReactionContextLoader,
) {

    suspend operator fun invoke(
        currentUserId: Uuid,
        feedItemId: Uuid,
        commentId: Uuid,
        content: RichTextDocument,
    ): RepositoryResult<FeedItemReply> {

        /*
         * FeedItem moet nog bestaan en actief zijn.
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

        /*
         * Parent-reaction ophalen.
         */
        val comment = when (
            val commentResult =
                reactionRepository.findById(
                    reactionId = commentId,
                )
        ) {
            is RepositoryResult.Success -> {
                commentResult.data
            }

            is RepositoryResult.Error -> {
                return RepositoryResult.Error(
                    error = commentResult.error,
                )
            }
        }

        /*
         * De comment moet bij hetzelfde FeedItem horen.
         */
        if (comment.feedItemId != feedItemId) {
            return RepositoryResult.Error(
                error = RepositoryError.BadRequest(
                    errors = listOf(
                        FieldError(
                            field = "commentId",
                            message =
                                "Comment does not belong to this feed item",
                        ),
                    ),
                    message = "Invalid comment",
                ),
            )
        }

        /*
         * Alleen reageren op een top-level comment.
         *
         * Een reply heeft zelf al parentReactionId != null
         * en mag dus geen nieuwe reply krijgen.
         */
        if (!comment.isComment) {
            return RepositoryResult.Error(
                error = RepositoryError.BadRequest(
                    errors = listOf(
                        FieldError(
                            field = "commentId",
                            message =
                                "Replies can only be created for comments",
                        ),
                    ),
                    message = "Invalid reply target",
                ),
            )
        }

        val contentJson =
            Json.encodeToString(
                value = content,
            )

        return when (
            val result =
                reactionRepository.createReply(
                    feedItemId = feedItemId,
                    parentReactionId = comment.id,
                    authorId = currentUserId,
                    contentJson = contentJson,
                    createdAt = Clock.System.now(),
                )
        ) {
            is RepositoryResult.Success -> {
                val reply =
                    reactionContextLoader.load(
                        currentUserId =
                            currentUserId,
                        reaction = result.data,
                    ) as? FeedItemReply
                        ?: return RepositoryResult.Error(
                            error =
                                RepositoryError.Internal(
                                    message =
                                        "Created reply could not be loaded",
                                ),
                        )

                RepositoryResult.Success(
                    data = reply,
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