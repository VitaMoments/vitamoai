package eu.vitamo.app.ui.user.search

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.media.model.MediaUrlResolver
import eu.vitamo.app.ui.components.infinitelist.InfiniteScrollList

@Composable
fun SearchUsersContent(
    state: SearchUsersState,
    listState: LazyListState,
    onEvent: (SearchUsersEvent) -> Unit,
    mediaUrlResolver: MediaUrlResolver,
    modifier: Modifier = Modifier,
    onUserClicked: (user: UserWithContext) -> Unit
) {
    InfiniteScrollList(
        modifier = modifier,
        listState = listState,
        showSearchBar = true,
        searchQuery = state.query,
        onSearchInput = { query ->
            onEvent(
                SearchUsersEvent.QueryChanged(query),
            )
        },
        onSearchButtonClick = {
            onEvent(
                SearchUsersEvent.SearchClicked,
            )
        },
        onLoadMore = {
            onEvent(
                SearchUsersEvent.LoadMore,
            )
        },
        onRetry = {
            onEvent(
                SearchUsersEvent.Retry,
            )
        },
        onLoadMoreRetry = {
            onEvent(
                SearchUsersEvent.RetryLoadMore,
            )
        },
        isInitialLoading = state.isInitialLoading,
        isLoadingMore = state.isLoadingMore,
        isEmpty = state.isEmpty,
        hasMore = state.nextOffset != null,
        initialError = state.initialError,
        loadMoreError = state.loadMoreError,
    ) {
        items(
            items = state.users,
            key = { user ->
                user.user.id
            },
        ) { user ->
            SearchUserItem(
                user = user,
                profileImageUrl = mediaUrlResolver.resolve(
                    user.user.profileImage,
                ),
                onClick = {
                    onUserClicked(user)
                },
            )
        }
    }
}