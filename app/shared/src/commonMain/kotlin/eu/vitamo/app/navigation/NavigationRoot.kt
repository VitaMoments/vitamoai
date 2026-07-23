package eu.vitamo.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import eu.vitamo.app.features.feed.ui.create.FeedCreateScreen
import eu.vitamo.app.features.feed.ui.detail.FeedDetailScreen
import eu.vitamo.app.features.feed.ui.edit.FeedEditScreen
import eu.vitamo.app.features.feed.ui.list.FeedListScreen
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
        AuthStatus.Authenticated -> MainDestination.FeedList

        AuthStatus.Unauthenticated ->
            parseAuthDeepLink(initialDeepLink)
                ?: AuthDestination.Login

        AuthStatus.Loading -> AuthDestination.Login
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

                        entry<MainDestination.FeedList> {
                            FeedListScreen(
                                onCreateClicked = {
                                    backStack.add(
                                        MainDestination.FeedCreate,
                                    )
                                },
                                onEditClicked = { id ->
                                    backStack.add(
                                        MainDestination.FeedEdit(id),
                                    )
                                },
                                onDetailClicked = { id ->
                                    backStack.add(
                                        MainDestination.FeedDetail(id),
                                    )
                                },
                            )
                        }

                        entry<MainDestination.FeedCreate> {
                            FeedCreateScreen(
                                onCreated = {
                                    backStack.setRoot(
                                        MainDestination.FeedList,
                                    )
                                },
                            )
                        }

                        entry<MainDestination.FeedEdit> { destination ->
                            FeedEditScreen(
                                uuid = destination.itemId,
                                onSaved = {
                                    backStack.setRoot(
                                        MainDestination.FeedList,
                                    )
                                },
                            )
                        }

                        entry<MainDestination.FeedDetail> { destination ->
                            FeedDetailScreen(
                                feedItemId = destination.itemId,
                                onBack = {
                                    backStack.setRoot(
                                        MainDestination.FeedList,
                                    )
                                },
                                onDeleted = {
                                    backStack.setRoot(
                                        MainDestination.FeedList,
                                    )
                                },
                            )
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
                                        MainDestination.FeedList,
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
                                        MainDestination.FeedList,
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