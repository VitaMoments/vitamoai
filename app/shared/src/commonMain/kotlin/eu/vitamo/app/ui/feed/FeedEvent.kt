package eu.vitamo.app.ui.feed

sealed interface FeedEvent {

    data object Refresh : FeedEvent

    data object LoadMore : FeedEvent

    data object Retry : FeedEvent

    data object RetryLoadMore : FeedEvent
}