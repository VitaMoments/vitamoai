package eu.vitamo.app.ui.feed

import eu.vitamo.app.features.media.model.PickedImage

data class CreatePostState(
    val images: List<PickedImage> = emptyList(),
    val text: String = "",
    val isSubmitting: Boolean = false,
) {

    val canAddImages: Boolean
        get() =
            images.size < MAX_IMAGES

    val remainingImageSlots: Int
        get() =
            (MAX_IMAGES - images.size)
                .coerceAtLeast(0)

    val canSubmit: Boolean
        get() =
            images.isNotEmpty() &&
                !isSubmitting

    companion object {
        const val MAX_IMAGES = 5
    }
}