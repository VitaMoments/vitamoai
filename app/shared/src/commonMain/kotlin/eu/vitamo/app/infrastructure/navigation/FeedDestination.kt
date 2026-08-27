package eu.vitamo.app.infrastructure.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface FeedDestination : AppDestination {

    @Serializable
    data object CreatePost : FeedDestination {
        override val route: String =
            "feed/create"

        override val title: String =
            "Nieuw bericht"
    }
}