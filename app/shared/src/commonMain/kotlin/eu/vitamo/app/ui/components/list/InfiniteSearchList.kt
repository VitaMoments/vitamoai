package eu.vitamo.app.ui.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import eu.vitamo.app.ui.theme.VitaDimensions
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Immutable
data class SearchFieldConfig(
    val query: String,
    val placeholder: String = "Zoeken",
    val enabled: Boolean = true,
)

@Composable
fun <T> InfiniteSearchList(
    items: List<T>,
    itemCount: Int = items.size,
    itemKey: (T) -> Any,
    hasMore: Boolean,
    isInitialLoading: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    search: SearchFieldConfig? = null,
    onSearchQueryChange: (String) -> Unit = {},
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    loadMoreThreshold: Int = DEFAULT_LOAD_MORE_THRESHOLD,
    itemContentType: (T) -> Any? = { null },
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 16.dp,
        vertical = 12.dp,
    ),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    emptyContent: @Composable BoxScope.() -> Unit = {
        DefaultEmptyContent()
    },
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
    require(itemCount in 0..items.size) {
        "itemCount must be between 0 and items.size. " +
                "itemCount=$itemCount, items.size=${items.size}"
    }

    require(loadMoreThreshold >= 0) {
        "loadMoreThreshold must be zero or greater."
    }

    val dimensions = VitaDimensions.current
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)

    LaunchedEffect(
        listState,
        itemCount,
        hasMore,
        isInitialLoading,
        isLoadingMore,
        loadMoreThreshold,
    ) {
        snapshotFlow {
            val lastVisibleItemIndex = listState
                .layoutInfo
                .visibleItemsInfo
                .lastOrNull()
                ?.index
                ?: NO_VISIBLE_ITEM_INDEX

            val loadMoreIndex = (
                    itemCount - 1 - loadMoreThreshold
                    ).coerceAtLeast(0)

            itemCount > 0 &&
                    hasMore &&
                    !isInitialLoading &&
                    !isLoadingMore &&
                    lastVisibleItemIndex >= loadMoreIndex
        }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                currentOnLoadMore()
            }
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        if (search != null) {
            SearchField(
                config = search,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensions.screenPadding,
                        vertical = dimensions.md,
                    ),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when {
                isInitialLoading && itemCount == 0 -> {
                    DefaultInitialLoadingContent(
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                errorMessage != null && itemCount == 0 -> {
                    DefaultErrorContent(
                        message = errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                itemCount == 0 -> {
                    emptyContent()
                }

                else -> {
                    InfiniteLazyColumn(
                        items = items,
                        itemCount = itemCount,
                        itemKey = itemKey,
                        itemContentType = itemContentType,
                        itemContent = itemContent,
                        listState = listState,
                        isLoadingMore = isLoadingMore,
                        errorMessage = errorMessage,
                        onRetry = onRetry,
                        contentPadding = contentPadding,
                        verticalArrangement = verticalArrangement,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    config: SearchFieldConfig,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = config.query,
        onValueChange = onQueryChange,
        modifier = modifier,
        enabled = config.enabled,
        placeholder = {
            Text(text = config.placeholder)
        },
        trailingIcon = if (config.query.isNotEmpty()) {
            {
                TextButton(
                    onClick = { onQueryChange("") },
                    enabled = config.enabled,
                ) {
                    Text(text = "Wissen")
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
        ),
        singleLine = true,
    )
}

@Composable
private fun <T> InfiniteLazyColumn(
    items: List<T>,
    itemCount: Int,
    itemKey: (T) -> Any,
    itemContentType: (T) -> Any?,
    itemContent: @Composable LazyItemScope.(T) -> Unit,
    listState: LazyListState,
    isLoadingMore: Boolean,
    errorMessage: String?,
    onRetry: (() -> Unit)?,
    contentPadding: PaddingValues,
    verticalArrangement: Arrangement.Vertical,
    modifier: Modifier = Modifier,
) {
    val dimensions = VitaDimensions.current

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
    ) {
        items(
            count = itemCount,
            key = { index -> itemKey(items[index]) },
            contentType = { index -> itemContentType(items[index]) },
        ) { index ->
            itemContent(items[index])
        }

        when {
            isLoadingMore -> {
                item(
                    key = LOAD_MORE_INDICATOR_KEY,
                    contentType = LOAD_MORE_INDICATOR_CONTENT_TYPE,
                ) {
                    LoadMoreIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimensions.lg),
                    )
                }
            }

            errorMessage != null -> {
                item(
                    key = LOAD_MORE_ERROR_KEY,
                    contentType = LOAD_MORE_ERROR_CONTENT_TYPE,
                ) {
                    LoadMoreErrorContent(
                        message = errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimensions.md),
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultInitialLoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DefaultEmptyContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Geen resultaten gevonden.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DefaultErrorContent(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "De gegevens konden niet worden geladen.",
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = message,
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 16.dp,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (onRetry != null) {
            Button(onClick = onRetry) {
                Text(text = "Opnieuw proberen")
            }
        }
    }
}

@Composable
private fun LoadMoreIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoadMoreErrorContent(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )

        if (onRetry != null) {
            TextButton(onClick = onRetry) {
                Text(text = "Opnieuw")
            }
        }
    }
}

private const val DEFAULT_LOAD_MORE_THRESHOLD = 4
private const val NO_VISIBLE_ITEM_INDEX = -1
private const val LOAD_MORE_INDICATOR_KEY = "infinite-list-load-more-indicator"
private const val LOAD_MORE_ERROR_KEY = "infinite-list-load-more-error"
private const val LOAD_MORE_INDICATOR_CONTENT_TYPE = "infinite-list-load-more-indicator"
private const val LOAD_MORE_ERROR_CONTENT_TYPE = "infinite-list-load-more-error"