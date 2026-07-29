package eu.vitamo.app.ui.user.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
import kotlin.uuid.Uuid
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ProfileState(),
    )

    val state: StateFlow<ProfileState> =
        _state.asStateFlow()

    private val _events = Channel<ProfileEvent>(
        capacity = Channel.BUFFERED,
    )

    val events = _events.receiveAsFlow()

    private var loadProfileJob: Job? = null

    fun loadMe() {
        val currentState = _state.value

        loadProfileJob?.cancel()

        loadProfileJob = viewModelScope.launch {
            when (
                val result = userRepository.getCurrentUser()
            ) {
                is RepositoryResult.Success -> {
                    _state.update { state ->
                        state.copy(
                            profile = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }
                }

                is RepositoryResult.Error -> {
                    handleProfileError(
                        userId = null,
                        error = result.error,
                    )
                }
            }
        }
    }

    fun loadProfile(
        userId: Uuid,
        forceRefresh: Boolean = false,
    ) {
        val currentState = _state.value

        val sameProfileAlreadyLoaded = currentState.profile?.user?.id == userId

        if (sameProfileAlreadyLoaded && !forceRefresh) {
            return
        }

        loadProfileJob?.cancel()

        loadProfileJob = viewModelScope.launch {
            val keepCurrentProfile =
                currentState.profile?.user?.id == userId

            _state.update { state ->
                state.copy(
                    profile = state.profile.takeIf {
                        keepCurrentProfile
                    },
                    isLoading = !keepCurrentProfile,
                    isRefreshing = keepCurrentProfile,
                    errorMessage = null,
                )
            }

            when (
                val result = userRepository.getUser(
                    userId = userId,
                )
            ) {
                is RepositoryResult.Success -> {
                    _state.update { state ->
                        state.copy(
                            profile = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }
                }

                is RepositoryResult.Error -> {
                    handleProfileError(
                        userId = userId,
                        error = result.error,
                    )
                }
            }
        }
    }

    fun retry() {
        val userId = _state.value.profile?.user?.id
            ?: return

        loadProfile(
            userId = userId,
            forceRefresh = true,
        )
    }

    fun refresh() {
        val userId = _state.value.profile?.user?.id
            ?: return

        loadProfile(
            userId = userId,
            forceRefresh = true,
        )
    }

    private suspend fun handleProfileError(
        userId: Uuid?,
        error: RepositoryError,
    ) {
        val message = error.toProfileErrorMessage()

        val currentState = _state.value

        val hasLoadedProfile =
            currentState.profile?.user?.id == userId

        if (hasLoadedProfile) {
            _state.update { state ->
                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                )
            }

            _events.send(
                ProfileEvent.ShowMessage(
                    message = message,
                ),
            )
        } else {
            _state.update { state ->
                state.copy(
                    profile = null,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = message,
                )
            }
        }
    }

    private fun RepositoryError.toProfileErrorMessage(): String =
        when (this) {
            is RepositoryError.Api -> {
                when (status) {
                    401 -> {
                        "Je sessie is verlopen. Log opnieuw in."
                    }

                    403 -> {
                        "Je hebt geen toegang tot dit profiel."
                    }

                    404 -> {
                        "Deze gebruiker kon niet worden gevonden."
                    }

                    else -> {
                        message.ifBlank {
                            "Het profiel kon niet worden geladen."
                        }
                    }
                }
            }

            is RepositoryError.Network -> {
                "Geen internetverbinding. Controleer je verbinding en probeer opnieuw."
            }

            is RepositoryError.Serialization -> {
                "Het antwoord van de server kon niet worden verwerkt."
            }

            is RepositoryError.NotFound -> {
                "Deze gebruiker kon niet worden gevonden."
            }

            is RepositoryError.Forbidden -> {
                "Je hebt geen toegang tot dit profiel."
            }

            is RepositoryError.Unauthorized -> {
                "Je sessie is verlopen. Log opnieuw in."
            }

            is RepositoryError.RequestLimitReached -> {
                "Je hebt te veel aanvragen gedaan. Probeer het later opnieuw."
            }

            is RepositoryError.Validation,
            is RepositoryError.BadRequest,
            is RepositoryError.Conflict,
                -> {
                message.ifBlank {
                    "Het profiel kon niet worden geladen."
                }
            }

            is RepositoryError.Internal,
            is RepositoryError.Unknown,
                -> {
                "Het profiel kon niet worden geladen. Probeer het later opnieuw."
            }
        }

    override fun onCleared() {
        loadProfileJob?.cancel()
        _events.close()

        super.onCleared()
    }
}