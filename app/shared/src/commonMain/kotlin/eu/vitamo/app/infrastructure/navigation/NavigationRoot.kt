package eu.vitamo.app.infrastructure.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import eu.vitamo.app.infrastructure.navigation.helper.parseAuthDeepLink
import eu.vitamo.app.infrastructure.navigation.helper.setRoot
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
import kotlinx.coroutines.launch

@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier,
    initialDeepLink: String? = null,
    authSessionCoordinator: AuthSessionCoordinator,
) {
    val coroutineScope = rememberCoroutineScope()
    val authState = authSessionCoordinator.state
        .collectAsState()
        .value

    var accessToken by remember {
        mutableStateOf<String?>(null)
    }

    var refreshToken by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(authState) {
        accessToken = authSessionCoordinator.getAccessCookie()
        refreshToken = authSessionCoordinator.getRefreshCookie()
    }

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
                            HomeScreen(
                                accessToken = accessToken,
                                refreshToken = refreshToken
                            )
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
                    onLogoutClicked = {
                        coroutineScope.launch {
                            authSessionCoordinator.signOut()
                            backStack.setRoot(AuthDestination.Login)
                        }
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