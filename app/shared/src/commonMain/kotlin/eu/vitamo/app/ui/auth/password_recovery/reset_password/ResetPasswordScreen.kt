package eu.vitamo.app.ui.auth.password_recovery.reset_password

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ResetPasswordScreen(
    token: String,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(token) {
        viewModel.onEvent(ResetPasswordEvent.TokenLoaded(token))
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        ResetPasswordContent(
            state = state,
            onEvent = viewModel::onEvent,
            onBackToLogin = onBackToLogin,
            modifier = Modifier.wrapContentSize(),
        )
    }
}