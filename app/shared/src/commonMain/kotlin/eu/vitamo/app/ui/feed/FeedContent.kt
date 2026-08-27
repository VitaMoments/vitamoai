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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import eu.vitamo.app.api.contracts.media.toMediaReference
import eu.vitamo.app.features.media.model.MediaUrlResolver
import eu.vitamo.app.ui.components.infinitelist.InfiniteScrollList
import eu.vitamo.app.ui.theme.VitaDimensions
import eu.vitamo.app.ui.user.components.UserAvatar

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
    }
}

@Composable
private fun FeedItemCard(
    item: FeedItem,
    imageLoader: ImageLoader,
    mediaUrlResolver: MediaUrlResolver,
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

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        dimensions.lg,
                    ),
            ) {
                Text(
                    text =
                        "${item.totalLikes} likes",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                )

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
}