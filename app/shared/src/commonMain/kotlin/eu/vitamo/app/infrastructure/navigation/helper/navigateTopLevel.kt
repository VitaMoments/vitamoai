package eu.vitamo.app.infrastructure.navigation.helper

import androidx.navigation3.runtime.NavBackStack
import eu.vitamo.app.infrastructure.navigation.AppDestination
import eu.vitamo.app.infrastructure.navigation.MainDestination

fun NavBackStack<AppDestination>.navigateTopLevel(destination: MainDestination) {
    if (lastOrNull() == destination) return
    removeAll { it is MainDestination }
    add(destination)
}