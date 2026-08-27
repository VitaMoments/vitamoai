package eu.vitamo.app.ui.feed

import eu.vitamo.app.api.contracts.feed.FeedItem

data class FeedState(
    val items: List<FeedItem> = emptyList(),

    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,

    val nextOffset: Long? = 0L,

    val initialError: String? = null,
    val loadMoreError: String? = null,
) {

    val isEmpty: Boolean
        get() =
            !isInitialLoading &&
                initialError == null &&
                items.isEmpty()
}