package eu.vitamo.app.ui.feed

import eu.vitamo.app.features.media.model.PickedImage

class CreatePostDraftStore {

    private var images: List<PickedImage> =
        emptyList()

    fun replaceImages(
        images: List<PickedImage>,
    ) {
        this.images = images
    }

    fun getImages(): List<PickedImage> =
        images

    fun clear() {
        images = emptyList()
    }
}