package eu.vitamo.app.ui.auth

import eu.vitamo.app.ui.auth.login.LoginViewModel
import eu.vitamo.app.ui.auth.password_recovery.forgot_password.ForgotPasswordViewModel
import eu.vitamo.app.ui.auth.password_recovery.reset_password.ResetPasswordViewModel
import eu.vitamo.app.ui.auth.registration.RegistrationViewModel
import eu.vitamo.app.ui.auth.verification.VerificationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val AuthUiKoinModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegistrationViewModel)
    viewModelOf(::VerificationViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
}