package eu.vitamo.app.ui.auth.password_recovery.forgot_password

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = koinViewModel())
{
    val state by viewModel.state.collectAsState()
    Box( modifier = modifier.fillMaxSize()) {
        ForgotPasswordContent(
            state = state,
            onEvent = viewModel::onEvent,
            onBackToLogin = onBackToLogin,
            modifier = Modifier.wrapContentSize())
    }
}