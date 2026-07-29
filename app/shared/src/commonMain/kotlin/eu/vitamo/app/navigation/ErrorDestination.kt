package eu.vitamo.app.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface ErrorDestination : AppDestination {
    override val title: String

    @Serializable
    data object Unavailable : ErrorDestination {
        override val route = "unavailable"
        override val title = "unavailable"
    }
}