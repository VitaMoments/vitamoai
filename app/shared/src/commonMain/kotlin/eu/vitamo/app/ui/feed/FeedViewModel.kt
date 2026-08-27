package eu.vitamo.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.features.feed.usecase.GetFeedUseCase
import eu.vitamo.app.features.media.model.PickedImage
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel(
    private val getFeedUseCase: GetFeedUseCase,
    private val createPostDraftStore: CreatePostDraftStore,
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            FeedState(),
        )

    val state =
        _state.asStateFlow()

    private val _effects =
        Channel<FeedEffect>(
            capacity = Channel.BUFFERED,
        )

    val effects =
        _effects.receiveAsFlow()

    init {
        loadInitial()
    }

    fun onEvent(
        event: FeedEvent,
    ) {
        when (event) {
            FeedEvent.Refresh -> {
                refresh()
            }

            FeedEvent.LoadMore -> {
                loadMore()
            }

            FeedEvent.Retry -> {
                loadInitial()
            }

            FeedEvent.RetryLoadMore -> {
                retryLoadMore()
            }
        }
    }

    /**
     * Wordt aangeroepen zodra de camera of galerij
     * afbeeldingen heeft teruggegeven.
     *
     * Daarna openen we pas het CreatePost-scherm.
     */
    fun onImagesPicked(
        images: List<PickedImage>,
    ) {
        if (images.isEmpty()) {
            return
        }

        val acceptedImages =
            images.take(MAX_POST_IMAGES)

        val rejectedImages =
            images.drop(MAX_POST_IMAGES)

        val previousImages =
            createPostDraftStore.getImages()

        createPostDraftStore.replaceImages(
            images = acceptedImages,
        )

        viewModelScope.launch {
            cleanupImages(
                images = previousImages,
            )

            cleanupImages(
                images = rejectedImages,
            )

            if (rejectedImages.isNotEmpty()) {
                _effects.send(
                    FeedEffect.ShowMessage(
                        message =
                            "Je kunt maximaal $MAX_POST_IMAGES foto's toevoegen.",
                    ),
                )
            }

            _effects.send(
                FeedEffect.OpenCreatePost,
            )
        }
    }

    fun onImagePicked(
        image: PickedImage,
    ) {
        val currentImages =
            createPostDraftStore.getImages()

        if (currentImages.size >= MAX_POST_IMAGES) {
            viewModelScope.launch {
                runCatching {
                    image.cleanup()
                }

                _effects.send(
                    FeedEffect.ShowMessage(
                        message =
                            "Je kunt maximaal $MAX_POST_IMAGES foto's toevoegen.",
                    ),
                )
            }

            return
        }

        val shouldOpenCreatePost =
            currentImages.isEmpty()

        createPostDraftStore.replaceImages(
            images =
                currentImages + image,
        )

        /*
         * Bij multi-select wordt onImagePicked meerdere keren
         * achter elkaar aangeroepen.
         *
         * We willen maar één keer navigeren.
         */
        if (shouldOpenCreatePost) {
            viewModelScope.launch {
                _effects.send(
                    FeedEffect.OpenCreatePost,
                )
            }
        }
    }

    fun onImagePickerError(
        message: String,
    ) {
        viewModelScope.launch {
            _effects.send(
                FeedEffect.ShowMessage(
                    message =
                        message.ifBlank {
                            "De afbeelding kon niet worden geselecteerd."
                        },
                ),
            )
        }
    }

    private fun loadInitial() {
        val currentState =
            state.value

        if (
            currentState.isInitialLoading ||
            currentState.isRefreshing
        ) {
            return
        }

        viewModelScope.launch {
            val hasItems =
                state.value.items.isNotEmpty()

            _state.update {
                it.copy(
                    isInitialLoading = !hasItems,
                    isRefreshing = hasItems,
                    isLoadingMore = false,
                    initialError = null,
                    loadMoreError = null,
                    nextOffset = 0L,
                )
            }

            when (
                val result =
                    getFeedUseCase(
                        limit = PAGE_SIZE,
                        offset = 0L,
                    )
            ) {
                is RepositoryResult.Success -> {
                    val page = result.data

                    _state.update {
                        it.copy(
                            items = page.items,
                            isInitialLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            nextOffset = page.nextOffset,
                            initialError = null,
                            loadMoreError = null,
                        )
                    }
                }

                is RepositoryResult.Error -> {
                    if (hasItems) {
                        _state.update {
                            it.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                            )
                        }

                        _effects.send(
                            FeedEffect.ShowMessage(
                                message =
                                    result.error.message,
                            ),
                        )
                    } else {
                        _state.update {
                            it.copy(
                                items = emptyList(),
                                isInitialLoading = false,
                                isRefreshing = false,
                                nextOffset = null,
                                initialError =
                                    result.error.message,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun refresh() {
        loadInitial()
    }

    private fun loadMore() {
        val currentState =
            state.value

        val offset =
            currentState.nextOffset
                ?: return

        if (
            currentState.isInitialLoading ||
            currentState.isRefreshing ||
            currentState.isLoadingMore
        ) {
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
                val result =
                    getFeedUseCase(
                        limit = PAGE_SIZE,
                        offset = offset,
                    )
            ) {
                is RepositoryResult.Success -> {
                    val page = result.data

                    _state.update { latestState ->
                        val existingIds =
                            latestState.items
                                .mapTo(
                                    mutableSetOf(),
                                ) {
                                    it.id
                                }

                        val newItems =
                            page.items.filter {
                                it.id !in existingIds
                            }

                        latestState.copy(
                            items =
                                latestState.items +
                                    newItems,
                            isLoadingMore = false,
                            nextOffset =
                                page.nextOffset,
                            loadMoreError = null,
                        )
                    }
                }

                is RepositoryResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            loadMoreError =
                                result.error.message,
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

    private suspend fun cleanupImages(
        images: List<PickedImage>,
    ) {
        images.forEach { image ->
            try {
                image.cleanup()
            } catch (cause: CancellationException) {
                throw cause
            } catch (_: Throwable) {
                // Best-effort cleanup.
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val MAX_POST_IMAGES = 5
    }
}