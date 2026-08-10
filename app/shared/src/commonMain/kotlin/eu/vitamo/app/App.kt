package eu.vitamo.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.navigation.NavigationRoot
import eu.vitamo.app.ui.app.AppViewModel
import eu.vitamo.app.ui.theme.AppTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    initialDeepLink: String? = null
) {
    val authSessionCoordinator: AuthSessionCoordinator = koinInject()
    val appViewModel: AppViewModel = koinViewModel()
    val appState by appViewModel.state.collectAsState()


    LaunchedEffect(authSessionCoordinator) {
        authSessionCoordinator.bootstrap()
    }

    AppTheme(mode = appState.themeMode) {
        NavigationRoot(
            initialDeepLink = initialDeepLink,
            authSessionCoordinator = authSessionCoordinator,
        )
    }
}