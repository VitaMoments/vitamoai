package eu.vitamo.app.ui.feed

sealed interface FeedEffect {

    data object OpenCreatePost : FeedEffect

    data class ShowMessage(
        val message: String,
    ) : FeedEffect
}