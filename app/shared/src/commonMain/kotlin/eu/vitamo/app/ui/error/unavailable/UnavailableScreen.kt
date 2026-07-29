package eu.vitamo.app.ui.error.unavailable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UnavailableScreen(
    viewModel: UnavailableViewModel = koinViewModel(),
) {
    val isRetrying by viewModel.isRetrying.collectAsState()

    UnavailableContent(
        isRetrying = isRetrying,
        onRetryClicked = viewModel::retry
    )
}