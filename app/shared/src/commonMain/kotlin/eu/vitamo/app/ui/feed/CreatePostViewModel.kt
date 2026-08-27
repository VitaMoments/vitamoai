package eu.vitamo.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.contracts.feed.FeedItemContent
import eu.vitamo.app.api.contracts.feed.request.CreatePostRequest
import eu.vitamo.app.features.feed.usecase.CreateFeedItemUseCase
import eu.vitamo.app.features.media.model.PickedImage
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreatePostViewModel(
    private val createFeedItemUseCase:
        CreateFeedItemUseCase,
    private val createPostDraftStore:
        CreatePostDraftStore,
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            CreatePostState(
                images =
                    createPostDraftStore
                        .getImages(),
            ),
        )

    val state =
        _state.asStateFlow()

    private val _effects =
        Channel<CreatePostEffect>(
            capacity = Channel.BUFFERED,
        )

    val effects =
        _effects.receiveAsFlow()

    fun onEvent(
        event: CreatePostEvent,
    ) {
        when (event) {
            is CreatePostEvent.TextChanged -> {
                onTextChanged(
                    text = event.text,
                )
            }

            is CreatePostEvent.ImagePicked -> {
                addImage(
                    image = event.image,
                )
            }

            is CreatePostEvent.RemoveImage -> {
                removeImage(
                    index = event.index,
                )
            }

            CreatePostEvent.Submit -> {
                submit()
            }

            CreatePostEvent.Cancel -> {
                cancel()
            }
        }
    }

    fun onImagePickerError(
        message: String,
    ) {
        viewModelScope.launch {
            _effects.send(
                CreatePostEffect.ShowMessage(
                    message =
                        message.ifBlank {
                            "De afbeelding kon niet worden geselecteerd."
                        },
                ),
            )
        }
    }

    private fun onTextChanged(
        text: String,
    ) {
        _state.update {
            it.copy(
                text = text,
            )
        }
    }

    private fun addImages(
        images: List<PickedImage>,
    ) {
        if (images.isEmpty()) {
            return
        }

        val currentState =
            state.value

        val acceptedImages =
            images.take(
                currentState
                    .remainingImageSlots,
            )

        val rejectedImages =
            images.drop(
                currentState
                    .remainingImageSlots,
            )

        val updatedImages =
            currentState.images +
                acceptedImages

        _state.update {
            it.copy(
                images = updatedImages,
            )
        }

        createPostDraftStore.replaceImages(
            images = updatedImages,
        )

        if (rejectedImages.isNotEmpty()) {
            viewModelScope.launch {
                cleanupImages(
                    images =
                        rejectedImages,
                )

                _effects.send(
                    CreatePostEffect.ShowMessage(
                        message =
                            "Je kunt maximaal ${CreatePostState.MAX_IMAGES} foto's toevoegen.",
                    ),
                )
            }
        }
    }

    private fun removeImage(
        index: Int,
    ) {
        val currentImages =
            state.value.images

        val image =
            currentImages.getOrNull(
                index = index,
            ) ?: return

        val updatedImages =
            currentImages.filterIndexed {
                    currentIndex,
                    _,
                ->
                currentIndex != index
            }

        _state.update {
            it.copy(
                images = updatedImages,
            )
        }

        createPostDraftStore.replaceImages(
            images = updatedImages,
        )

        viewModelScope.launch {
            cleanupImages(
                images = listOf(image),
            )
        }
    }

    private fun submit() {
        val currentState =
            state.value

        if (
            currentState.isSubmitting ||
            currentState.images.isEmpty()
        ) {
            return
        }

        val text =
            currentState.text
                .trim()
                .takeIf(
                    String::isNotEmpty,
                )

        val request =
            CreatePostRequest(
                content =
                    FeedItemContent(
                        /*
                         * Tijdelijk totdat we jouw exacte
                         * RichTextDocument-constructor hebben.
                         */
                        title = text,
                        message = null,
                    ),
            )

        val images =
            currentState.images

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSubmitting = true,
                )
            }

            try {
                when (
                    val result =
                        createFeedItemUseCase(
                            request = request,
                            images = images,
                        )
                ) {
                    is RepositoryResult.Success -> {
                        cleanupImages(
                            images = images,
                        )

                        createPostDraftStore.clear()

                        _state.value =
                            CreatePostState()

                        _effects.send(
                            CreatePostEffect.PostCreated,
                        )
                    }

                    is RepositoryResult.Error -> {
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                            )
                        }

                        _effects.send(
                            CreatePostEffect.ShowMessage(
                                message =
                                    result.error.message,
                            ),
                        )
                    }
                }
            } catch (
                cause: CancellationException,
            ) {
                throw cause
            } catch (cause: Throwable) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                    )
                }

                _effects.send(
                    CreatePostEffect.ShowMessage(
                        message =
                            cause.message
                                ?: "Het bericht kon niet worden geplaatst.",
                    ),
                )
            }
        }
    }

    private fun cancel() {
        if (state.value.isSubmitting) {
            return
        }

        val images =
            state.value.images

        createPostDraftStore.clear()

        _state.value =
            CreatePostState()

        viewModelScope.launch {
            cleanupImages(
                images = images,
            )

            _effects.send(
                CreatePostEffect.Close,
            )
        }
    }

    private suspend fun cleanupImages(
        images: List<PickedImage>,
    ) {
        images.forEach { image ->
            try {
                image.cleanup()
            } catch (
                cause: CancellationException,
            ) {
                throw cause
            } catch (_: Throwable) {
                // Best-effort cleanup.
            }
        }
    }
    private fun addImage(
        image: PickedImage,
    ) {
        val currentState =
            state.value

        if (
            currentState.images.size >=
            CreatePostState.MAX_IMAGES
        ) {
            viewModelScope.launch {
                runCatching {
                    image.cleanup()
                }

                _effects.send(
                    CreatePostEffect.ShowMessage(
                        message =
                            "Je kunt maximaal ${CreatePostState.MAX_IMAGES} foto's toevoegen.",
                    ),
                )
            }

            return
        }

        val updatedImages =
            currentState.images + image

        _state.update {
            it.copy(
                images = updatedImages,
            )
        }

        createPostDraftStore.replaceImages(
            images = updatedImages,
        )
    }
}