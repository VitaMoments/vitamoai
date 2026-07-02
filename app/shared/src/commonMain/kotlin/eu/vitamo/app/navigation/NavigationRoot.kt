package eu.vitamo.app.navigation

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
import eu.vitamo.app.features.feed.ui.create.FeedCreateScreen
import eu.vitamo.app.features.feed.ui.detail.FeedDetailScreen
import eu.vitamo.app.features.feed.ui.edit.FeedEditScreen
import eu.vitamo.app.features.feed.ui.list.FeedListScreen
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
    val authState = authSessionCoordinator.state.collectAsState().value
    if (authState == AuthStatus.Loading) {
        return
    }

    val initialDestination = when (authState) {
        AuthStatus.Authenticated -> MainDestination.FeedList
        AuthStatus.Unauthenticated -> parseAuthDeepLink(initialDeepLink) ?: AuthDestination.Login
        AuthStatus.Loading -> AuthDestination.Login
    }
    key(initialDestination) {
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
                entry<MainDestination.FeedList> {
                    FeedListScreen(
                        onCreateClicked = { backStack.add(MainDestination.FeedCreate) },
                        onEditClicked = { id -> backStack.add(MainDestination.FeedEdit(id)) },
                        onDetailClicked = { id -> backStack.add(MainDestination.FeedDetail(id)) },
                    )
                }
                entry<MainDestination.FeedCreate> {
                    FeedCreateScreen(
                        onCreated = {
                            backStack.setRoot(MainDestination.FeedList)
                        },
                    )
                }
                entry<MainDestination.FeedEdit> { key ->
                    FeedEditScreen(
                        uuid = key.itemId,
                        onSaved = { backStack.setRoot(MainDestination.FeedList) },
                    )
                }
                entry<MainDestination.FeedDetail> { key ->
                    FeedDetailScreen(
                        feedItemId = key.itemId,
                        onBack = { backStack.setRoot(MainDestination.FeedList) },
                        onDeleted = { backStack.setRoot(MainDestination.FeedList) },
                    )
                }

    //            Auth destinations are not part of the main navigation graph, but we need to declare them here so that they can be saved and restored properly when navigating to them from the main graph.
                entry<AuthDestination.Login> { LoginScreen(
                    onLoginSuccess = {
                        backStack.setRoot(MainDestination.FeedList)
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
                            backStack.setRoot(MainDestination.FeedList)
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
}