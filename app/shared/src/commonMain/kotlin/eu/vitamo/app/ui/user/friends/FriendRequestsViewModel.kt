package eu.vitamo.app.ui.user.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.user.friendship.usecase.AcceptFriendRequestUseCase
import eu.vitamo.app.features.user.friendship.usecase.GetFriendRequestsUseCase
import eu.vitamo.app.features.user.friendship.usecase.RejectFriendRequestUseCase
import eu.vitamo.app.features.user.friendship.usecase.RevokeFriendRequestUseCase
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class FriendRequestsViewModel(
    private val getFriendRequestsUseCase: GetFriendRequestsUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val rejectFriendRequestUseCase: RejectFriendRequestUseCase,
    private val revokeFriendRequestUseCase: RevokeFriendRequestUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(
        FriendRequestsState()
    )

    val state = _state.asStateFlow()

    private val _effects = Channel<FriendRequestsEffect>(
        capacity = Channel.BUFFERED
    )

    val effects = _effects.receiveAsFlow()

    private var searchJob: Job? = null

    init {
        loadInitial()
    }

    fun onEvent(
        event: FriendRequestsEvent,
    ) {
        when (event) {

            FriendRequestsEvent.LoadMore -> {
                loadMore()
            }

            FriendRequestsEvent.Retry -> {
                loadInitial()
            }

            FriendRequestsEvent.RetryLoadMore -> {
                retryLoadMore()
            }
            is FriendRequestsEvent.AcceptFriendRequest -> {
                acceptFriendRequest(
                    userId = event.userId,
                    friendshipId = event.friendshipId
                )
            }
            is FriendRequestsEvent.RejectFriendRequest -> {
                rejectFriendRequest(
                    userId = event.userId,
                    friendshipId = event.friendshipId
                )
            }
            is FriendRequestsEvent.RevokeFriendRequest -> {
                revokeFriendRequest(
                    userId = event.userId,
                    friendshipId = event.friendshipId
                )
            }
        }
    }

    private fun loadMore() {
        val currentState = state.value

        val nextOffset = currentState.nextOffset
            ?: return

        if (currentState.isLoadingMore) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingMore = true,
                    loadMoreError = null,
                )
            }

            when (
                val result = getFriendRequestsUseCase(
                    limit = PAGE_SIZE,
                    offset = nextOffset,
                )
            ) {
                is RepositoryResult.Success -> {
                    val page = result.data

                    _state.update {
                        it.copy(
                            users = it.users + page.items,
                            isLoadingMore = false,
                            nextOffset = page.nextOffset,
                        )
                    }
                }

                is RepositoryResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            loadMoreError = result.error.message,
                        )
                    }
                }
            }
        }
    }

    private fun retryLoadMore() {
        _state.update {
            it.copy(
                loadMoreError = null,
            )
        }

        loadMore()
    }

    private fun acceptFriendRequest(
        userId: Uuid,
        friendshipId: Uuid,
    ) {
        performFriendshipAction(
            userId = userId,
            successMessage = "Vriendschapsverzoek geaccepteerd.",
        ) {
            acceptFriendRequestUseCase(
                friendshipId = friendshipId,
            )
        }
    }

    private fun rejectFriendRequest(
        userId: Uuid,
        friendshipId: Uuid,
    ) {
        performFriendshipAction(
            userId = userId,
            successMessage = "Vriendschapsverzoek afgewezen.",
        ) {
            rejectFriendRequestUseCase(
                friendshipId = friendshipId,
            )
        }
    }

    private fun revokeFriendRequest(
        userId: Uuid,
        friendshipId: Uuid,
    ) {
        performFriendshipAction(
            userId = userId,
            successMessage = "Vriendschapsverzoek geannuleerd.",
        ) {
            revokeFriendRequestUseCase(
                friendshipId = friendshipId,
            )
        }
    }

    private fun updateUser(
        updatedUser: UserWithContext,
    ) {
        _state.update { currentState ->
            currentState.copy(
                users = currentState.users.map { user ->
                    if (
                        user.user.id == updatedUser.user.id
                    ) {
                        updatedUser
                    } else {
                        user
                    }
                },
            )
        }
    }
    private fun setFriendshipActionLoading(
        userId: Uuid,
        isLoading: Boolean,
    ) {
        _state.update { currentState ->
            currentState.copy(
                friendshipActionsInProgress = if (isLoading) {
                    currentState.friendshipActionsInProgress + userId
                } else {
                    currentState.friendshipActionsInProgress - userId
                },
            )
        }
    }

    private fun performFriendshipAction(
        userId: Uuid,
        successMessage: String,
        action: suspend () -> RepositoryResult<UserWithContext>,
    ) {
        if (state.value.isFriendshipActionInProgress(userId)) {
            return
        }

        viewModelScope.launch {
            setFriendshipActionLoading(
                userId = userId,
                isLoading = true,
            )

            when (val result = action()) {
                is RepositoryResult.Success -> {
                    updateUser(result.data)

                    _effects.send(
                        FriendRequestsEffect.ShowMessage(
                            successMessage,
                        ),
                    )
                }

                is RepositoryResult.Error -> {
                    _effects.send(
                        FriendRequestsEffect.ShowMessage(
                            result.error.message,
                        ),
                    )
                }
            }

            setFriendshipActionLoading(
                userId = userId,
                isLoading = false,
            )
        }
    }

    private fun loadInitial(
        scrollToTop: Boolean = false,
    ) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isInitialLoading = true,
                    isLoadingMore = false,
                    initialError = null,
                    loadMoreError = null,
                    nextOffset = 0L,
                )
            }

            if (scrollToTop) {
                _effects.send(
                    FriendRequestsEffect.ScrollToTop,
                )
            }

            when (
                val result = getFriendRequestsUseCase(
                    limit = PAGE_SIZE,
                    offset = 0L,
                )
            ) {
                is RepositoryResult.Success -> {
                    val page = result.data

                    _state.update {
                        it.copy(
                            users = page.items,
                            isInitialLoading = false,
                            nextOffset = page.nextOffset,
                            initialError = null,
                        )
                    }
                }

                is RepositoryResult.Error -> {
                    _state.update {
                        it.copy(
                            users = emptyList(),
                            isInitialLoading = false,
                            initialError = "Gebruikers konden niet worden geladen.",
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}