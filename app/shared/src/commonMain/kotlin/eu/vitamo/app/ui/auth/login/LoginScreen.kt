package eu.vitamo.app.ui.auth.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onEmailNotVerified: (emailAddress: String) -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LoginContent(
        state = state,
        onEmailAddressChanged = viewModel::onEmailAddressChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onLoginClick = viewModel::login,
        onRegisterClick = onRegisterClick,
        onForgotPasswordClick = onForgotPasswordClick
    )

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.LoginSuccess -> {
                    onLoginSuccess()
                }

                is LoginEvent.EmailNotVerified -> {
                    onEmailNotVerified(event.emailAddress)
                }
            }
        }
    }
}