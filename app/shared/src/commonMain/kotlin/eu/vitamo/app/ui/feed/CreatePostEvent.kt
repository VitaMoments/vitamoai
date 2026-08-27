package eu.vitamo.app.ui.feed

import eu.vitamo.app.features.media.model.PickedImage

sealed interface CreatePostEvent {

    data class TextChanged(
        val text: String,
    ) : CreatePostEvent

    data class ImagePicked(
        val image: PickedImage,
    ) : CreatePostEvent

    data class RemoveImage(
        val index: Int,
    ) : CreatePostEvent

    data object Submit : CreatePostEvent

    data object Cancel : CreatePostEvent
}