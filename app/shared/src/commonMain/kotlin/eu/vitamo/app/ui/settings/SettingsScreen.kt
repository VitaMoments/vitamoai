package eu.vitamo.app.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LifecycleEventEffect(
        event = Lifecycle.Event.ON_RESUME,
    ) {
        viewModel.refreshPermissions()
    }

    LaunchedEffect(
        state.errorMessage,
    ) {
        val message = state.errorMessage
            ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message = message,
        )

        viewModel.clearError()
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        SettingsContent(
            state = state,
            onThemeModeSelected = viewModel::setThemeMode,
            onPermissionSelected =
                viewModel::requestPermission,
            onTestNotification = viewModel::sendTestNotification,
            modifier = Modifier.fillMaxSize(),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(
                Alignment.BottomCenter,
            ),
        )
    }
}