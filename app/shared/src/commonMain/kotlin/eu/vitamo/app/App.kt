package eu.vitamo.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import eu.vitamo.app.navigation.NavigationRoot

@Composable
fun App(
    initialDeepLink: String? = null
) {
    MaterialTheme {
        NavigationRoot(initialDeepLink = initialDeepLink)
    }
}