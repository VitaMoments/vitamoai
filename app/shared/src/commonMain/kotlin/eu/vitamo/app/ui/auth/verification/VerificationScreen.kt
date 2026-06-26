package eu.vitamo.app.ui.auth.verification

import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VerificationScreen(
    email: String,
    viewModel: VerificationViewModel = koinViewModel(),
    onVerificationSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    VerifyEmailContent(
        email = email,
        state = state,
        onCodeChanged = viewModel::onCodeChanged,
        onVerifyClick = {
            viewModel.verifyEmail(email)
        },
        onResendCodeClick = {
            viewModel.resendCode(email)
        },
        onBackToLoginClick = onBackToLogin,
    )

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                VerifyEmailEvent.VerificationSuccess -> {
                    onVerificationSuccess()
                }
            }
        }
    }
}