package eu.vitamo.app.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface MainDestination : AppDestination {
    override val title: String

    @Serializable
    data object Home : MainDestination {
        override val route = "home"
        override val title = "Home"
    }

    @Serializable
    data object FeedList : MainDestination {
        override val route = "feed_list"
        override val title = "Feed"
    }

    @Serializable
    data object FeedCreate : MainDestination {
        override val route = "feed_create"
        override val title = "Nieuw feed item"
    }

    @Serializable
    data class FeedDetail(val itemId: String) : MainDestination {
        override val route = "feed_detail"
        override val title = "Feed item"
    }

    @Serializable
    data class FeedEdit(val itemId: String) : MainDestination {
        override val route = "feed_edit"
        override val title = "Bewerk feed item"
    }
}