package eu.vitamo.app.ui.user.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import kotlin.uuid.Uuid
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    userId: Uuid? = null,
    onBackClicked: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.loadProfile(
                userId = userId,
            )
        } else {
            viewModel.loadMe()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                    )
                }
            }
        }
    }

    ProfileContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBackClicked = onBackClicked,
        onRetryClicked = viewModel::retry,
    )
}