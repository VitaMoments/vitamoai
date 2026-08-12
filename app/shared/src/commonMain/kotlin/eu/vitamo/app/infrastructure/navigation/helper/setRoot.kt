package eu.vitamo.app.infrastructure.navigation.helper

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import eu.vitamo.app.infrastructure.navigation.AppDestination

fun NavBackStack<NavKey>.setRoot(destination: AppDestination) {
    // Clear the back stack and set the new root destination
    clear()
    add(destination)
}