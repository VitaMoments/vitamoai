package eu.vitamo.app.ui.components.infinitelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InfiniteScrollList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    showSearchBar: Boolean = false,
    searchQuery: String = "",
    onSearchInput: ((String) -> Unit)? = null,
    onSearchButtonClick: ((String) -> Unit)? = null,
    onLoadMore: () -> Unit,
    onRetry: (() -> Unit)? = null,
    onLoadMoreRetry: (() -> Unit)? = null,
    isInitialLoading: Boolean = false,
    isLoadingMore: Boolean = false,
    isEmpty: Boolean = false,
    hasMore: Boolean = true,
    initialError: String? = null,
    loadMoreError: String? = null,
    loadMoreThreshold: Int = 3,
    emptyContent: @Composable () -> Unit = {
        DefaultEmptyContent()
    },
    content: LazyListScope.() -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        if (showSearchBar) {
            InfiniteScrollSearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    onSearchInput?.invoke(query)
                },
                onSearch = {
                    onSearchButtonClick?.invoke(searchQuery)
                },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when {
                isInitialLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                initialError != null -> {
                    ErrorContent(
                        message = initialError,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                isEmpty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        emptyContent()
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        content()

                        if (isLoadingMore) {
                            item(
                                key = "infinite-list-loading-more",
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        if (loadMoreError != null) {
                            item(
                                key = "infinite-list-load-more-error",
                            ) {
                                ErrorContent(
                                    message = loadMoreError,
                                    onRetry = onLoadMoreRetry,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                )
                            }
                        }
                    }

                    InfiniteScrollHandler(
                        listState = listState,
                        hasMore = hasMore,
                        isLoadingMore = isLoadingMore,
                        hasLoadMoreError = loadMoreError != null,
                        threshold = loadMoreThreshold,
                        onLoadMore = onLoadMore,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfiniteScrollHandler(
    listState: LazyListState,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    hasLoadMoreError: Boolean,
    threshold: Int,
    onLoadMore: () -> Unit,
) {
    val shouldLoadMore by remember(
        listState,
        threshold,
    ) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo

            val totalItems = layoutInfo.totalItemsCount

            val lastVisibleItemIndex = layoutInfo
                .visibleItemsInfo
                .lastOrNull()
                ?.index
                ?: return@derivedStateOf false

            totalItems > 0 &&
                    lastVisibleItemIndex >= totalItems - 1 - threshold
        }
    }

    LaunchedEffect(
        shouldLoadMore,
        hasMore,
        isLoadingMore,
        hasLoadMoreError,
    ) {
        if (
            shouldLoadMore &&
            hasMore &&
            !isLoadingMore &&
            !hasLoadMoreError
        ) {
            onLoadMore()
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = message,
        )

        if (onRetry != null) {
            Button(
                onClick = onRetry,
            ) {
                Text("Opnieuw proberen")
            }
        }
    }
}

@Composable
private fun DefaultEmptyContent() {
    Text(
        text = "Geen resultaten gevonden",
        modifier = Modifier.padding(24.dp),
    )
}