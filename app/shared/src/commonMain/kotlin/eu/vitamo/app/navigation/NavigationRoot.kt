package eu.vitamo.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import eu.vitamo.app.navigation.helper.parseAuthDeepLink
import eu.vitamo.app.navigation.helper.setRoot
import eu.vitamo.app.ui.auth.login.LoginScreen
import eu.vitamo.app.ui.auth.password_recovery.forgot_password.ForgotPasswordScreen
import eu.vitamo.app.ui.auth.password_recovery.reset_password.ResetPasswordScreen
import eu.vitamo.app.ui.auth.registration.RegistrationScreen
import eu.vitamo.app.ui.auth.verification.VerificationScreen
import eu.vitamo.app.ui.home.HomeScreen

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier,
    initialDeepLink: String? = null
) {
    val initialDestination = parseAuthDeepLink(initialDeepLink) ?: AuthDestination.Login
    val backStack = rememberNavBackStack(appNavSavedStateConfiguration, initialDestination)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<MainDestination.Home> { HomeScreen() }

//            Auth destinations are not part of the main navigation graph, but we need to declare them here so that they can be saved and restored properly when navigating to them from the main graph.
            entry<AuthDestination.Login> { LoginScreen(
                onLoginSuccess = {
                    backStack.setRoot(MainDestination.Home)
                },
                onRegisterClick = {
                    backStack.add(AuthDestination.Register)
                },
                onEmailNotVerified = { emailAddress ->
                    backStack.add(
                        AuthDestination.VerifyEmailAddress(
                            emailAddress = emailAddress
                        )
                    )
                },
                onForgotPasswordClick = {
                    backStack.add(AuthDestination.ForgotPassword)
                }
            )}
            entry<AuthDestination.Register> {
                RegistrationScreen(
                    onRegisterSuccess = { email ->
                        backStack.add(
                            AuthDestination.VerifyEmailAddress(
                                emailAddress = email,
                            )
                        )
                    },
                    onLoginClick = {
                        backStack.setRoot(AuthDestination.Login)
                    },
                )
            }
            entry<AuthDestination.VerifyEmailAddress> { key ->
                VerificationScreen(
                    email = key.emailAddress,
                    onVerificationSuccess = {
                        backStack.setRoot(MainDestination.Home)
                    },
                    onBackToLogin = {
                        backStack.setRoot(AuthDestination.Login)
                    },
                )
            }
            entry<AuthDestination.ForgotPassword> {
                ForgotPasswordScreen(
                    onBackToLogin = {
                        backStack.setRoot(AuthDestination.Login)
                    }
                )
            }
            entry<AuthDestination.ResetPassword> { key ->
                ResetPasswordScreen(
                    token = key.token,
                    onBackToLogin = {
                        backStack.setRoot(AuthDestination.Login)
                    },
                )
            }
        }
    )
}