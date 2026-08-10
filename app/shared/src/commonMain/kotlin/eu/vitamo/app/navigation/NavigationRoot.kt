package eu.vitamo.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import eu.vitamo.app.navigation.helper.parseAuthDeepLink
import eu.vitamo.app.navigation.helper.setRoot
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.auth.AuthStatus
import eu.vitamo.app.ui.auth.login.LoginScreen
import eu.vitamo.app.ui.auth.password_recovery.forgot_password.ForgotPasswordScreen
import eu.vitamo.app.ui.auth.password_recovery.reset_password.ResetPasswordScreen
import eu.vitamo.app.ui.auth.registration.RegistrationScreen
import eu.vitamo.app.ui.auth.verification.VerificationScreen
import eu.vitamo.app.ui.home.HomeScreen
import eu.vitamo.app.ui.settings.SettingsScreen
import eu.vitamo.app.ui.user.profile.ProfileScreen
import eu.vitamo.app.ui.user.search.SearchUsersScreen
import io.ktor.util.logging.Logger

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier,
    initialDeepLink: String? = null,
    authSessionCoordinator: AuthSessionCoordinator,
) {
    val authState = authSessionCoordinator.state
        .collectAsState()
        .value

    if (authState == AuthStatus.Loading) {
        return
    }

    val initialDestination = when (authState) {
        AuthStatus.Authenticated -> MainDestination.Home

        AuthStatus.Unauthenticated ->
            parseAuthDeepLink(initialDeepLink)
                ?: AuthDestination.Login

        AuthStatus.Loading -> AuthDestination.Login
        is AuthStatus.Unavailable -> AuthDestination.Login
    }

    key(initialDestination) {
        val backStack = rememberNavBackStack(
            appNavSavedStateConfiguration,
            initialDestination,
        )

        val navigationContent: @Composable (Modifier) -> Unit =
            { navigationModifier ->
                NavDisplay(
                    backStack = backStack,
                    modifier = navigationModifier,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = entryProvider {
                        entry<MainDestination.Home> {
                            HomeScreen()
                        }

                        entry<MainDestination.Profile> {
                            ProfileScreen(
                                onBackClicked = {
                                    backStack.setRoot(
                                        MainDestination.Home,
                                    )
                                }
                            )
                        }

                        entry<MainDestination.Settings> {
                            SettingsScreen()
                        }

                        entry<MainDestination.Users> {
                            SearchUsersScreen()
                        }


                        /*
                         * Auth destinations zitten bewust in dezelfde
                         * entryProvider, zodat Navigation 3 ze kan opslaan
                         * en herstellen. Ze worden niet binnen AppScaffold
                         * weergegeven.
                         */
                        entry<AuthDestination.Login> {
                            LoginScreen(
                                onLoginSuccess = {
                                    backStack.setRoot(
                                        MainDestination.Home,
                                    )
                                },
                                onRegisterClick = {
                                    backStack.add(
                                        AuthDestination.Register,
                                    )
                                },
                                onEmailNotVerified = { emailAddress ->
                                    backStack.add(
                                        AuthDestination.VerifyEmailAddress(
                                            emailAddress = emailAddress,
                                        ),
                                    )
                                },
                                onForgotPasswordClick = {
                                    backStack.add(
                                        AuthDestination.ForgotPassword,
                                    )
                                },
                            )
                        }

                        entry<AuthDestination.Register> {
                            RegistrationScreen(
                                onRegisterSuccess = { email ->
                                    backStack.add(
                                        AuthDestination
                                            .VerifyEmailAddress(
                                                emailAddress = email,
                                            ),
                                    )
                                },
                                onLoginClick = {
                                    backStack.setRoot(
                                        AuthDestination.Login,
                                    )
                                },
                            )
                        }

                        entry<AuthDestination.VerifyEmailAddress> {
                                destination ->
                            VerificationScreen(
                                email = destination.emailAddress,
                                onVerificationSuccess = {
                                    backStack.setRoot(
                                        MainDestination.Home,
                                    )
                                },
                                onBackToLogin = {
                                    backStack.setRoot(
                                        AuthDestination.Login,
                                    )
                                },
                            )
                        }

                        entry<AuthDestination.ForgotPassword> {
                            ForgotPasswordScreen(
                                onBackToLogin = {
                                    backStack.setRoot(
                                        AuthDestination.Login,
                                    )
                                },
                            )
                        }

                        entry<AuthDestination.ResetPassword> {
                                destination ->
                            ResetPasswordScreen(
                                token = destination.token,
                                onBackToLogin = {
                                    backStack.setRoot(
                                        AuthDestination.Login,
                                    )
                                },
                            )
                        }
                    },
                )
            }

        when (val currentDestination = backStack.lastOrNull()) {
            is MainDestination -> {
                AppScaffold(
                    currentDestination = currentDestination,
                    onDestinationSelected = { destination ->
                        backStack.setRoot(destination)
                    },
                    modifier = modifier,
                ) { innerPadding ->
                    navigationContent(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                }
            }

            else -> {
                /*
                 * Login, registratie, verificatie en password recovery
                 * worden zonder appdrawer en appbar weergegeven.
                 */
                navigationContent(modifier)
            }
        }
    }
}