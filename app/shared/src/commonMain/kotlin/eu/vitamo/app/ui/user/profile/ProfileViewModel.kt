package eu.vitamo.app.ui.user.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.features.media.model.PickedImage
import eu.vitamo.app.features.user.repository.UserRepository
import eu.vitamo.app.repository.RepositoryError
import eu.vitamo.app.repository.RepositoryResult
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
    private var uploadProfileImageJob: Job? = null

    /**
     * Haalt het profiel van de ingelogde gebruiker op.
     *
     * Bij een normale eerste load wordt opnieuw laden overgeslagen wanneer
     * het profiel al aanwezig is. Met forceRefresh wordt het profiel altijd
     * opnieuw bij de server opgehaald.
     */
    fun loadProfile(
        forceRefresh: Boolean = false,
    ) {
        val currentState = _state.value
        val hasProfile = currentState.profile != null

        if (hasProfile && !forceRefresh) {
            return
        }

        if (currentState.isLoading || currentState.isRefreshing) {
            return
        }

        loadProfileJob?.cancel()

        loadProfileJob = viewModelScope.launch {
            _state.update { state ->
                state.copy(
                    isLoading = state.profile == null,
                    isRefreshing = state.profile != null,
                    errorMessage = null,
                )
            }

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
                    handleProfileLoadError(
                        error = result.error,
                    )
                }
            }
        }
    }

    /**
     * Probeert een mislukte eerste profiel-load opnieuw.
     */
    fun retry() {
        loadProfile(
            forceRefresh = true,
        )
    }

    /**
     * Vernieuwt een reeds geladen profiel.
     *
     * Tijdens deze call blijft het bestaande profiel zichtbaar en wordt
     * isRefreshing op true gezet.
     */
    fun refresh() {
        loadProfile(
            forceRefresh = true,
        )
    }

    /**
     * Uploadt de geselecteerde afbeelding en stelt deze in één serverrequest
     * in als profielfoto van de ingelogde gebruiker.
     *
     * De UserRepository stuurt hiervoor:
     *
     * PUT /users/me/profile-image
     *
     * De server verwerkt de afbeelding, maakt de MediaAsset aan, koppelt deze
     * aan de huidige gebruiker en stuurt de bijgewerkte UserWithContext terug.
     */
    fun updateProfileImage(
        image: PickedImage,
    ) {
        if (_state.value.isProfileImageUploading) {
            cleanupUnusedImage(image)
            return
        }

        uploadProfileImageJob?.cancel()

        uploadProfileImageJob = viewModelScope.launch {
            _state.update { state ->
                state.copy(
                    isProfileImageUploading = true,
                )
            }

            when (
                val result = userRepository.updateProfileImage(
                    image = image,
                )
            ) {
                is RepositoryResult.Success -> {
                    _state.update { state ->
                        state.copy(
                            profile = result.data,
                            isProfileImageUploading = false,
                            errorMessage = null,
                        )
                    }

                    _events.send(
                        ProfileEvent.ShowMessage(
                            message = "Je profielfoto is bijgewerkt.",
                        ),
                    )
                }

                is RepositoryResult.Error -> {
                    _state.update { state ->
                        state.copy(
                            isProfileImageUploading = false,
                        )
                    }

                    _events.send(
                        ProfileEvent.ShowMessage(
                            message = result.error
                                .toProfileImageErrorMessage(),
                        ),
                    )
                }
            }
        }
    }

    /**
     * Wordt aangeroepen wanneer de camera of galerij-picker zelf een fout geeft,
     * dus voordat er een repositorycall is uitgevoerd.
     */
    fun onImagePickerError(
        message: String,
    ) {
        viewModelScope.launch {
            _events.send(
                ProfileEvent.ShowMessage(
                    message = message.ifBlank {
                        "De afbeelding kon niet worden geselecteerd."
                    },
                ),
            )
        }
    }

    private suspend fun handleProfileLoadError(
        error: RepositoryError,
    ) {
        val hasExistingProfile =
            _state.value.profile != null

        val message =
            error.toProfileLoadErrorMessage()

        if (hasExistingProfile) {
            /*
             * Bij een refreshfout blijft het bestaande profiel zichtbaar.
             * De fout wordt via een snackbar getoond.
             */
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
            /*
             * Bij een fout tijdens de eerste load is er nog geen profiel om te
             * tonen. De fout komt daarom in de ProfileState terecht.
             */
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

    private fun cleanupUnusedImage(
        image: PickedImage,
    ) {
        viewModelScope.launch {
            runCatching {
                image.cleanup()
            }
        }
    }

    private fun RepositoryError.toProfileLoadErrorMessage(): String =
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
                        "Je profiel kon niet worden gevonden."
                    }

                    429 -> {
                        "Je hebt te veel aanvragen gedaan. Probeer het later opnieuw."
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

            is RepositoryError.Unauthorized -> {
                "Je sessie is verlopen. Log opnieuw in."
            }

            is RepositoryError.Forbidden -> {
                "Je hebt geen toegang tot dit profiel."
            }

            is RepositoryError.NotFound -> {
                "Je profiel kon niet worden gevonden."
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

    private fun RepositoryError.toProfileImageErrorMessage(): String =
        when (this) {
            is RepositoryError.Api -> {
                when (status) {
                    400 -> {
                        message.ifBlank {
                            "De geselecteerde afbeelding is ongeldig."
                        }
                    }

                    401 -> {
                        "Je sessie is verlopen. Log opnieuw in."
                    }

                    403 -> {
                        "Je mag deze profielfoto niet wijzigen."
                    }

                    404 -> {
                        "Je profiel kon niet worden gevonden."
                    }

                    413 -> {
                        "De geselecteerde afbeelding is te groot."
                    }

                    415 -> {
                        "Dit type afbeelding wordt niet ondersteund."
                    }

                    429 -> {
                        "Je hebt te veel afbeeldingen geüpload. Probeer het later opnieuw."
                    }

                    else -> {
                        message.ifBlank {
                            "De profielfoto kon niet worden bijgewerkt."
                        }
                    }
                }
            }

            is RepositoryError.Network -> {
                "Geen internetverbinding. De profielfoto kon niet worden geüpload."
            }

            is RepositoryError.Serialization -> {
                "Het antwoord van de server kon niet worden verwerkt."
            }

            is RepositoryError.BadRequest,
            is RepositoryError.Validation,
                -> {
                message.ifBlank {
                    "De geselecteerde afbeelding is ongeldig."
                }
            }

            is RepositoryError.Unauthorized -> {
                "Je sessie is verlopen. Log opnieuw in."
            }

            is RepositoryError.Forbidden -> {
                "Je mag deze profielfoto niet wijzigen."
            }

            is RepositoryError.NotFound -> {
                "Je profiel kon niet worden gevonden."
            }

            is RepositoryError.RequestLimitReached -> {
                "Je hebt te veel afbeeldingen geüpload. Probeer het later opnieuw."
            }

            is RepositoryError.Conflict -> {
                message.ifBlank {
                    "De profielfoto kon door een conflict niet worden bijgewerkt."
                }
            }

            is RepositoryError.Internal,
            is RepositoryError.Unknown,
                -> {
                "De profielfoto kon niet worden bijgewerkt. Probeer het later opnieuw."
            }
        }

    override fun onCleared() {
        loadProfileJob?.cancel()
        uploadProfileImageJob?.cancel()
        _events.close()

        super.onCleared()
    }
}