package eu.vitamo.app.features.media.image

import eu.vitamo.app.features.media.model.PickedImage

expect class ImageCompressor() {
    suspend fun compress(
        image: PickedImage,
        maxDimension: Int,
        quality: Int,
        maxBytes: Long,
    ): CompressedImage
}