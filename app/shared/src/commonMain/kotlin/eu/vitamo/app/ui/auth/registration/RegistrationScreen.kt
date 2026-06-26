package eu.vitamo.app.ui.auth.registration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel = koinViewModel(),
    onRegisterSuccess: (email: String) -> Unit,
    onLoginClick: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    RegistrationContent(
        state = state,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onDisplayNameChanged = viewModel::onDisplayNameChanged,
        onFirstNameChanged = viewModel::onFirstNameChanged,
        onLastNameChanged = viewModel::onLastNameChanged,
        onAliasChanged = viewModel::onAliasChanged,
        onBirthDateChanged = viewModel::onBirthDateChanged,
        onRegisterClick = viewModel::register,
        onLoginClick = onLoginClick,
    )

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is RegistrationEvent.RegistrationSuccess -> {
                    onRegisterSuccess(event.email)
                }
            }
        }
    }
}