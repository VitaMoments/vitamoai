package eu.vitamo.app.navigation.helper

import androidx.navigation3.runtime.NavBackStack
import eu.vitamo.app.navigation.AppDestination
import eu.vitamo.app.navigation.MainDestination

fun NavBackStack<AppDestination>.navigateTopLevel(destination: MainDestination) {
    if (lastOrNull() == destination) return
    removeAll { it is MainDestination }
    add(destination)
}