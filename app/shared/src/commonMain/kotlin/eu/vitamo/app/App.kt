package eu.vitamo.app

import androidx.compose.runtime.*
import eu.vitamo.app.features.device.service.FirebaseInstallationIdSynchronizer
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.infrastructure.navigation.NavigationRoot
import eu.vitamo.app.network.auth.AuthStatus
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
    val firebaseInstallationIdSynchronizer: FirebaseInstallationIdSynchronizer = koinInject()

    LaunchedEffect(
        authSessionCoordinator,
        firebaseInstallationIdSynchronizer,
    ) {
        authSessionCoordinator.bootstrap()

        if (authSessionCoordinator.state.value == AuthStatus.Authenticated) {
            firebaseInstallationIdSynchronizer.synchronizeStored()
        }
    }

    AppTheme(mode = appState.themeMode) {
        NavigationRoot(
            initialDeepLink = initialDeepLink,
            authSessionCoordinator = authSessionCoordinator,
        )
    }
}