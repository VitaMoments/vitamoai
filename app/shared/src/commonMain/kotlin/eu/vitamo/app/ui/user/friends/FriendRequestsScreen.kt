package eu.vitamo.app.ui.user.friends

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import eu.vitamo.app.features.media.model.MediaUrlResolver
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
fun FriendRequestsScreen(
    initialUserId: Uuid? = null,
    viewModel: FriendRequestsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val mediaUrlResolver =
        koinInject<MediaUrlResolver>()

    val listState =
        rememberLazyListState()

    LaunchedEffect(
        viewModel,
    ) {
        viewModel.effects.collect { effect ->
            when (effect) {
                FriendRequestsEffect.ScrollToTop -> {
                    listState.scrollToItem(
                        index = 0,
                    )
                }

                is FriendRequestsEffect.ShowMessage -> {
                    // Snackbar/toast kan hier later worden toegevoegd.
                }
            }
        }
    }

    LaunchedEffect(
        initialUserId,
        state.users,
    ) {
        val userId =
            initialUserId
                ?: return@LaunchedEffect

        val index =
            state.users.indexOfFirst { user ->
                user.user.id == userId
            }

        if (index >= 0) {
            listState.animateScrollToItem(
                index = index,
            )
        }
    }

    FriendRequestsContent(
        state = state,
        listState = listState,
        onEvent = viewModel::onEvent,
        mediaUrlResolver = mediaUrlResolver,
        onAccept = { userId ->
            val friendshipId =
                state.users
                    .firstOrNull { user ->
                        user.user.id == userId
                    }
                    ?.friendshipContext
                    ?.friendshipId
                    ?: return@FriendRequestsContent

            viewModel.onEvent(
                FriendRequestsEvent.AcceptFriendRequest(
                    userId = userId,
                    friendshipId = friendshipId,
                ),
            )
        },
        onReject = { userId ->
            val friendshipId =
                state.users
                    .firstOrNull { user ->
                        user.user.id == userId
                    }
                    ?.friendshipContext
                    ?.friendshipId
                    ?: return@FriendRequestsContent

            viewModel.onEvent(
                FriendRequestsEvent.RejectFriendRequest(
                    userId = userId,
                    friendshipId = friendshipId,
                ),
            )
        },
        onRevoke = { userId ->
            val friendshipId =
                state.users
                    .firstOrNull { user ->
                        user.user.id == userId
                    }
                    ?.friendshipContext
                    ?.friendshipId
                    ?: return@FriendRequestsContent

            viewModel.onEvent(
                FriendRequestsEvent.RevokeFriendRequest(
                    userId = userId,
                    friendshipId = friendshipId,
                ),
            )
        },
    )
}