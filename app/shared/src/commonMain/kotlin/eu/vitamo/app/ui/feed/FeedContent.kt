package eu.vitamo.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.FeedItemComment
import eu.vitamo.app.api.contracts.feed.FeedItemPermission
import eu.vitamo.app.api.contracts.feed.FeedItemReply
import eu.vitamo.app.api.contracts.media.toMediaReference
import eu.vitamo.app.features.media.model.MediaUrlResolver
import eu.vitamo.app.ui.components.infinitelist.InfiniteScrollList
import eu.vitamo.app.ui.theme.VitaDimensions
import eu.vitamo.app.ui.user.components.UserAvatar
import kotlin.uuid.Uuid

@Composable
fun FeedContent(
    state: FeedState,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    imageLoader: ImageLoader,
    mediaUrlResolver: MediaUrlResolver,
    onEvent: (FeedEvent) -> Unit,
    onCreatePostClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        InfiniteScrollList(
            modifier =
                Modifier.fillMaxSize(),
            listState = listState,
            showSearchBar = false,
            onLoadMore = {
                onEvent(
                    FeedEvent.LoadMore,
                )
            },
            onRetry = {
                onEvent(
                    FeedEvent.Retry,
                )
            },
            onLoadMoreRetry = {
                onEvent(
                    FeedEvent.RetryLoadMore,
                )
            },
            isInitialLoading =
                state.isInitialLoading,
            isLoadingMore =
                state.isLoadingMore,
            isEmpty =
                state.isEmpty,
            hasMore =
                state.nextOffset != null,
            initialError =
                state.initialError,
            loadMoreError =
                state.loadMoreError,
        ) {
            items(
                items = state.items,
                key = { item ->
                    item.id
                },
            ) { item ->
                FeedItemCard(
                    item = item,
                    imageLoader =
                        imageLoader,
                    mediaUrlResolver =
                        mediaUrlResolver,
                    feedItemLikePending =
                        state.isFeedItemLikePending(
                            feedItemId = item.id,
                        ),
                    reactionLikePending = { reactionId ->
                        state.isReactionLikePending(
                            reactionId = reactionId,
                        )
                    },
                    onEvent = onEvent,
                )
            }
        }

        if (state.isRefreshing) {
            LinearProgressIndicator(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(
                            Alignment.TopCenter,
                        ),
            )
        }

        FloatingActionButton(
            onClick =
                onCreatePostClicked,
            modifier =
                Modifier
                    .align(
                        Alignment.BottomEnd,
                    )
                    .padding(20.dp),
        ) {
            Text(
                text = "+",
                style =
                    MaterialTheme
                        .typography
                        .headlineMedium,
            )
        }

        SnackbarHost(
            hostState =
                snackbarHostState,
            modifier =
                Modifier.align(
                    Alignment.BottomCenter,
                ),
        )

        state.composerTarget?.let { target ->
            ReactionComposerDialog(
                target = target,
                text = state.composerText,
                isSubmitting =
                    state.isSubmittingReaction,
                onTextChanged = { value ->
                    onEvent(
                        FeedEvent.ComposerTextChanged(
                            value = value,
                        ),
                    )
                },
                onSubmit = {
                    onEvent(
                        FeedEvent.SubmitComposer,
                    )
                },
                onDismiss = {
                    onEvent(
                        FeedEvent.DismissComposer,
                    )
                },
            )
        }
    }
}

@Composable
private fun FeedItemCard(
    item: FeedItem,
    imageLoader: ImageLoader,
    mediaUrlResolver: MediaUrlResolver,
    feedItemLikePending: Boolean,
    reactionLikePending: (Uuid) -> Boolean,
    onEvent: (FeedEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions =
        VitaDimensions.current

    val author =
        item.metaData.author

    val assets =
        item.content.assets.orEmpty()

    Card(
        modifier =
            modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        dimensions.cardPaddingLarge,
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    dimensions.md,
                ),
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(
                        dimensions.md,
                    ),
            ) {
                UserAvatar(
                    user = author,
                    profileImageUrl =
                        mediaUrlResolver.resolve(
                            author.profileImage,
                        ),
                    showLoadingIndicator = false,
                    modifier =
                        Modifier.size(48.dp),
                )

                Column(
                    modifier =
                        Modifier.weight(1f),
                ) {
                    Text(
                        text =
                            author.displayName,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis,
                    )

                    Text(
                        text =
                            item.metaData
                                .createdAt
                                .toString(),
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                    )
                }
            }

            item.content.title
                ?.takeIf(
                    String::isNotBlank,
                )
                ?.let { text ->
                    Text(
                        text = text,
                        style =
                            MaterialTheme
                                .typography
                                .bodyLarge,
                    )
                }

            if (assets.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            dimensions.sm,
                        ),
                ) {
                    items(
                        items = assets,
                    ) { media ->
                        val imageUrl =
                            mediaUrlResolver.resolve(
                                media.toMediaReference(),
                            )

                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                imageLoader =
                                    imageLoader,
                                contentDescription =
                                    null,
                                contentScale =
                                    ContentScale.Crop,
                                modifier =
                                    Modifier
                                        .size(220.dp)
                                        .aspectRatio(1f),
                            )
                        }
                    }
                }
            }

            FeedItemActions(
                item = item,
                likePending =
                    feedItemLikePending,
                onEvent = onEvent,
            )

            if (
                item.reactions.items.isNotEmpty()
            ) {
                HorizontalDivider()

                Column(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            dimensions.md,
                        ),
                ) {
                    item.reactions.items
                        .forEach { comment ->
                            FeedCommentContent(
                                feedItemId =
                                    item.id,
                                comment =
                                    comment,
                                mediaUrlResolver =
                                    mediaUrlResolver,
                                reactionLikePending =
                                    reactionLikePending,
                                onEvent =
                                    onEvent,
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun FeedItemActions(
    item: FeedItem,
    likePending: Boolean,
    onEvent: (FeedEvent) -> Unit,
) {
    val dimensions =
        VitaDimensions.current

    val likedByMe =
        item.interactionContext.likedByMe

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                dimensions.md,
            ),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        if (item.canLike) {
            TextButton(
                onClick = {
                    onEvent(
                        FeedEvent.ToggleFeedItemLike(
                            feedItemId =
                                item.id,
                        ),
                    )
                },
                enabled =
                    !likePending,
            ) {
                Text(
                    text = buildString {
                        append(
                            if (likedByMe) {
                                "♥"
                            } else {
                                "♡"
                            },
                        )

                        append(" ")
                        append(item.totalLikes)
                    },
                )
            }
        } else {
            Text(
                text =
                    "${item.totalLikes} likes",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
            )
        }

        /*
         * canReply is op FeedItem-niveau momenteel
         * feitelijk de canComment-permission.
         */
        if (item.canReply) {
            TextButton(
                onClick = {
                    onEvent(
                        FeedEvent.OpenCommentComposer(
                            feedItemId =
                                item.id,
                        ),
                    )
                },
            ) {
                Text(
                    text =
                        "Reageren · ${item.totalComments}",
                )
            }
        } else {
            /*
             * Wanneer reageren niet is toegestaan,
             * tonen we alleen het aantal.
             */
            Text(
                text =
                    "${item.totalComments} reacties",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
            )
        }
    }
}

@Composable
private fun FeedCommentContent(
    feedItemId: Uuid,
    comment: FeedItemComment,
    mediaUrlResolver: MediaUrlResolver,
    reactionLikePending: (Uuid) -> Boolean,
    onEvent: (FeedEvent) -> Unit,
) {
    val dimensions =
        VitaDimensions.current

    Column(
        modifier =
            Modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(
                dimensions.xs,
            ),
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    dimensions.sm,
                ),
        ) {
            UserAvatar(
                user = comment.author,
                profileImageUrl =
                    mediaUrlResolver.resolve(
                        comment.author.profileImage,
                    ),
                showLoadingIndicator = false,
                modifier =
                    Modifier.size(32.dp),
            )

            Text(
                text =
                    comment.author.displayName,
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.Bold,
            )
        }

        /*
         * Tijdelijk simpele weergave.
         * Als je al een RichTextDocument composable hebt,
         * kun je deze Text daarmee vervangen.
         */
        Text(
            text =
                comment.content.toString(),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
        )

        CommentActions(
            feedItemId =
                feedItemId,
            comment =
                comment,
            likePending =
                reactionLikePending(
                    comment.id,
                ),
            onEvent =
                onEvent,
        )

        if (
            comment.pagedReplies.items
                .isNotEmpty()
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = dimensions.lg,
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        dimensions.sm,
                    ),
            ) {
                comment.pagedReplies.items
                    .forEach { reply ->
                        FeedReplyContent(
                            feedItemId =
                                feedItemId,
                            reply =
                                reply,
                            mediaUrlResolver =
                                mediaUrlResolver,
                            likePending =
                                reactionLikePending(
                                    reply.id,
                                ),
                            onEvent =
                                onEvent,
                        )
                    }
            }
        }
    }
}

@Composable
private fun CommentActions(
    feedItemId: Uuid,
    comment: FeedItemComment,
    likePending: Boolean,
    onEvent: (FeedEvent) -> Unit,
) {
    val canLike =
        comment.interactionContext
            .permissions
            .canLike ==
                FeedItemPermission.GRANTED

    val canReply =
        comment.interactionContext
            .permissions
            .canReply ==
                FeedItemPermission.GRANTED

    val likedByMe =
        comment.interactionContext
            .likedByMe

    Row(
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        if (canLike) {
            TextButton(
                onClick = {
                    onEvent(
                        FeedEvent.ToggleReactionLike(
                            feedItemId =
                                feedItemId,
                            reactionId =
                                comment.id,
                        ),
                    )
                },
                enabled =
                    !likePending,
            ) {
                Text(
                    text = buildString {
                        append(
                            if (likedByMe) {
                                "♥"
                            } else {
                                "♡"
                            },
                        )

                        append(" ")
                        append(
                            comment.likes.total,
                        )
                    },
                )
            }
        } else {
            Text(
                text =
                    "${comment.likes.total} likes",
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
            )
        }

        if (canReply) {
            TextButton(
                onClick = {
                    onEvent(
                        FeedEvent.OpenReplyComposer(
                            feedItemId =
                                feedItemId,
                            commentId =
                                comment.id,
                        ),
                    )
                },
            ) {
                Text(
                    text =
                        "Reageren · ${comment.pagedReplies.total}",
                )
            }
        } else {
            Text(
                text =
                    "${comment.pagedReplies.total} reacties",
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
            )
        }
    }
}

@Composable
private fun FeedReplyContent(
    feedItemId: Uuid,
    reply: FeedItemReply,
    mediaUrlResolver: MediaUrlResolver,
    likePending: Boolean,
    onEvent: (FeedEvent) -> Unit,
) {
    val dimensions =
        VitaDimensions.current

    val canLike =
        reply.interactionContext
            .permissions
            .canLike ==
                FeedItemPermission.GRANTED

    val likedByMe =
        reply.interactionContext
            .likedByMe

    Column(
        modifier =
            Modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(
                dimensions.xs,
            ),
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    dimensions.sm,
                ),
        ) {
            UserAvatar(
                user = reply.author,
                profileImageUrl =
                    mediaUrlResolver.resolve(
                        reply.author.profileImage,
                    ),
                showLoadingIndicator = false,
                modifier =
                    Modifier.size(28.dp),
            )

            Text(
                text =
                    reply.author.displayName,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                fontWeight =
                    FontWeight.Bold,
            )
        }

        Text(
            text =
                reply.content.toString(),
            style =
                MaterialTheme
                    .typography
                    .bodySmall,
        )

        if (canLike) {
            TextButton(
                onClick = {
                    onEvent(
                        FeedEvent.ToggleReactionLike(
                            feedItemId =
                                feedItemId,
                            reactionId =
                                reply.id,
                        ),
                    )
                },
                enabled =
                    !likePending,
            ) {
                Text(
                    text = buildString {
                        append(
                            if (likedByMe) {
                                "♥"
                            } else {
                                "♡"
                            },
                        )

                        append(" ")
                        append(
                            reply.likes.total,
                        )
                    },
                )
            }
        } else {
            Text(
                text =
                    "${reply.likes.total} likes",
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
            )
        }
    }
}

@Composable
private fun ReactionComposerDialog(
    target: FeedComposerTarget,
    text: String,
    isSubmitting: Boolean,
    onTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        title = {
            Text(
                text =
                    when (target) {
                        is FeedComposerTarget.Comment ->
                            "Reactie plaatsen"

                        is FeedComposerTarget.Reply ->
                            "Antwoord plaatsen"
                    },
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange =
                    onTextChanged,
                modifier =
                    Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text =
                            "Schrijf een reactie…",
                    )
                },
                enabled =
                    !isSubmitting,
                minLines = 3,
                maxLines = 6,
            )
        },
        confirmButton = {
            TextButton(
                onClick =
                    onSubmit,
                enabled =
                    text.isNotBlank() &&
                            !isSubmitting,
            ) {
                Text(
                    text =
                        if (isSubmitting) {
                            "Plaatsen…"
                        } else {
                            "Plaatsen"
                        },
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick =
                    onDismiss,
                enabled =
                    !isSubmitting,
            ) {
                Text(
                    text = "Annuleren",
                )
            }
        },
    )
}