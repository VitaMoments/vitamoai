package eu.vitamo.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.navigation.NavigationRoot
import org.koin.compose.koinInject

@Composable
fun App(
    initialDeepLink: String? = null
) {
    val authSessionCoordinator: AuthSessionCoordinator = koinInject()
    LaunchedEffect(authSessionCoordinator) {
        authSessionCoordinator.bootstrap()
    }

    MaterialTheme {
        NavigationRoot(
            initialDeepLink = initialDeepLink,
            authSessionCoordinator = authSessionCoordinator,
        )
    }
}