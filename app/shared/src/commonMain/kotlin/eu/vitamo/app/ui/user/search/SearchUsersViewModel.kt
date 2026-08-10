package eu.vitamo.app.ui.user.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.contracts.user.UserWithContext
import eu.vitamo.app.features.user.friendship.usecase.AcceptFriendRequestUseCase
import eu.vitamo.app.features.user.friendship.usecase.RejectFriendRequestUseCase
import eu.vitamo.app.features.user.friendship.usecase.RemoveFriendshipUseCase
import eu.vitamo.app.features.user.friendship.usecase.RevokeFriendRequestUseCase
import eu.vitamo.app.features.user.friendship.usecase.SendFriendRequestUseCase
import eu.vitamo.app.features.user.search.usecase.SearchUsersUseCase
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class SearchUsersViewModel(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val sendFriendRequestUseCase : SendFriendRequestUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val rejectFriendRequestUseCase: RejectFriendRequestUseCase,
    private val revokeFriendRequestUseCase: RevokeFriendRequestUseCase,
    private val removeFriendshipUseCase: RemoveFriendshipUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        SearchUsersState(),
    )

    val state = _state.asStateFlow()

    private val _effects = Channel<SearchUsersEffect>(
        capacity = Channel.BUFFERED,
    )

    val effects = _effects.receiveAsFlow()

    private var searchJob: Job? = null

    init {
        loadInitial()
    }

    fun onEvent(
        event: SearchUsersEvent,
    ) {
        when (event) {
            is SearchUsersEvent.QueryChanged -> {
                onQueryChanged(event.query)
            }

            SearchUsersEvent.SearchClicked -> {
                search()
            }

            SearchUsersEvent.LoadMore -> {
                loadMore()
            }

            SearchUsersEvent.Retry -> {
                loadInitial()
            }

            SearchUsersEvent.RetryLoadMore -> {
                retryLoadMore()
            }

            is SearchUsersEvent.SendFriendRequest -> {
                sendFriendRequest(event.userId)
            }
            is SearchUsersEvent.AcceptFriendRequest -> {
                acceptFriendRequest(
                    userId = event.userId,
                    friendshipId = event.friendshipId
                )
            }
            is SearchUsersEvent.RejectFriendRequest -> {
                rejectFriendRequest(
                    userId = event.userId,
                    friendshipId = event.friendshipId
                )
            }
            is SearchUsersEvent.RevokeFriendRequest -> {
                revokeFriendRequest(
                    userId = event.userId,
                    friendshipId = event.friendshipId
                )
            }

            is SearchUsersEvent.RemoveFriendship -> {
                removeFriendship(
                    userId = event.userId,
                    friendshipId = event.friendshipId
                )
            }
        }
    }

    private fun onQueryChanged(
        query: String,
    ) {
        _state.update {
            it.copy(
                query = query,
            )
        }
    }

    private fun search() {
        loadInitial(
            scrollToTop = true,
        )
    }

    private fun loadInitial(
        scrollToTop: Boolean = false,
    ) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            val query = state.value.query
                .trim()
                .takeIf(String::isNotBlank)

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
                    SearchUsersEffect.ScrollToTop,
                )
            }

            when (
                val result = searchUsersUseCase(
                    query = query,
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
                val result = searchUsersUseCase(
                    query = currentState.query,
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

    private fun sendFriendRequest(
        userId: Uuid,
    ) {
        if (
            state.value.isFriendshipActionInProgress(userId)
        ) {
            return
        }

        viewModelScope.launch {
            setFriendshipActionLoading(
                userId = userId,
                isLoading = true,
            )

            when (
                val result = sendFriendRequestUseCase(
                    userId = userId,
                )
            ) {
                is RepositoryResult.Success -> {
                    updateUser(
                        updatedUser = result.data,
                    )

                    _effects.send(
                        SearchUsersEffect.ShowMessage(
                            message = "Vriendschapsverzoek verzonden.",
                        ),
                    )
                }

                is RepositoryResult.Error -> {
                    _effects.send(
                        SearchUsersEffect.ShowMessage(
                            message = result.error.message,
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

    private fun removeFriendship(
        userId: Uuid,
        friendshipId: Uuid,
    ) {
        performFriendshipAction(
            userId = userId,
            successMessage = "Vriendschap verwijderd.",
        ) {
            removeFriendshipUseCase(
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
                        SearchUsersEffect.ShowMessage(
                            successMessage,
                        ),
                    )
                }

                is RepositoryResult.Error -> {
                    _effects.send(
                        SearchUsersEffect.ShowMessage(
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

    private companion object {
        const val PAGE_SIZE = 20
    }
}