package eu.vitamo.app.infrastructure.navigation

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
    data object Profile : MainDestination {
        override val route = "profile"
        override val title = "Profile"
    }

    @Serializable
    data object Users : MainDestination {
        override val route: String = "users"
        override val title: String = "Users"
    }

    @Serializable
    data object Settings : MainDestination {
        override val route = "settings"
        override val title = "Settings"
    }
}