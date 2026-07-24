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
}