package eu.vitamo.app.ui.user.search

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil3.ImageLoader
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.media.model.MediaUrlResolver
import eu.vitamo.app.ui.user.components.dialog.UserInfoDialog
import kotlinx.coroutines.flow.collect
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
fun SearchUsersScreen(
    viewModel: SearchUsersViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val mediaUrlResolver = koinInject<MediaUrlResolver>()
    val listState = rememberLazyListState()

    var selectedUserId by remember {
        mutableStateOf<Uuid?>(null)
    }

    val selectedUser = selectedUserId?.let { userId ->
        state.users.firstOrNull { user ->
            user.user.id == userId
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SearchUsersEffect.ScrollToTop -> {
                    listState.scrollToItem(0)
                }

                is SearchUsersEffect.ShowMessage -> {
                    // snackbar tonen
                }
            }
        }
    }

    SearchUsersContent(
        state = state,
        listState = listState,
        onEvent = viewModel::onEvent,
        mediaUrlResolver = mediaUrlResolver,
        onUserClicked = { user ->
            selectedUserId = user.user.id
        },
    )
    selectedUser?.let { user ->
        val friendshipId = user.friendshipContext.friendshipId

        UserInfoDialog(
            userContext = user,
            profileImageUrl = mediaUrlResolver.resolve(
                user.user.profileImage,
            ),
            isFriendshipActionLoading =
                state.isFriendshipActionInProgress(user.user.id),

            onSendFriendRequest = {
                viewModel.onEvent(
                    SearchUsersEvent.SendFriendRequest(
                        userId = user.user.id,
                    ),
                )
            },

            onCancelFriendRequest = {
                friendshipId?.let {
                    viewModel.onEvent(
                        SearchUsersEvent.RevokeFriendRequest(
                            userId = user.user.id,
                            friendshipId = it,
                        ),
                    )
                }
            },

            onAcceptFriendRequest = {
                friendshipId?.let {
                    viewModel.onEvent(
                        SearchUsersEvent.AcceptFriendRequest(
                            userId = user.user.id,
                            friendshipId = it,
                        ),
                    )
                }
            },

            onRejectFriendRequest = {
                friendshipId?.let {
                    viewModel.onEvent(
                        SearchUsersEvent.RejectFriendRequest(
                            userId = user.user.id,
                            friendshipId = it,
                        ),
                    )
                }
            },

            onRemoveFriendship = {
                friendshipId?.let {
                    viewModel.onEvent(
                        SearchUsersEvent.RemoveFriendship(
                            userId = user.user.id,
                            friendshipId = it,
                        ),
                    )
                }
            },

            onDismiss = {
                selectedUserId = null
            },
        )
    }
}