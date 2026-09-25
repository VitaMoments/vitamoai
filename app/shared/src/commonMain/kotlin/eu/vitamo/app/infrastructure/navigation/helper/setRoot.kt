package eu.vitamo.app.infrastructure.navigation.helper

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import eu.vitamo.app.infrastructure.navigation.AppDestination
import eu.vitamo.app.infrastructure.navigation.AuthDestination
import eu.vitamo.app.infrastructure.navigation.ErrorDestination
import eu.vitamo.app.infrastructure.navigation.FeedDestination
import eu.vitamo.app.infrastructure.navigation.MainDestination

fun NavBackStack<NavKey>.setRoot(destination: AppDestination) {
    // Clear the back stack and set the new root destination
    clear()
    if (destination is MainDestination && destination != MainDestination.Home) {
        add(MainDestination.Home)
    }
    add(destination)
}