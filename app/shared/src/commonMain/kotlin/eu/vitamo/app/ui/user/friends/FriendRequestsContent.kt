package eu.vitamo.app.ui.user.friends

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.vitamo.app.features.media.model.MediaUrlResolver
import eu.vitamo.app.ui.components.infinitelist.InfiniteScrollList
import kotlin.uuid.Uuid

@Composable
fun FriendRequestsContent(
    state: FriendRequestsState,
    listState: LazyListState,
    onEvent: (FriendRequestsEvent) -> Unit,
    mediaUrlResolver: MediaUrlResolver,
    modifier: Modifier = Modifier,
    onAccept: (userId: Uuid) -> Unit,
    onReject: (userId: Uuid) -> Unit,
    onRevoke: (userId: Uuid) -> Unit
) {
    InfiniteScrollList(
        modifier = modifier,
        listState = listState,
        showSearchBar = false,
        onLoadMore = {
            onEvent(FriendRequestsEvent.LoadMore)
        },
        onRetry = {
            onEvent(FriendRequestsEvent.Retry)
        },
        onLoadMoreRetry = {
            onEvent(FriendRequestsEvent.RetryLoadMore)
        },
        isInitialLoading = state.isInitialLoading,
        isLoadingMore = state.isLoadingMore,
        isEmpty = state.isEmpty,
        hasMore = state.nextOffset != null,
        initialError = state.initialError,
        loadMoreError = state.loadMoreError
    ) {
        items(
            items = state.users,
            key = { user ->
                user.user.id
            },
        ) { user ->
            FriendRequestItem(
                user = user,
                profileImageUrl = mediaUrlResolver.resolve(
                    user.user.profileImage
                ),
                onAccept = onAccept,
                onReject = onReject,
                onRevoke = onRevoke
            )
        }
    }
}