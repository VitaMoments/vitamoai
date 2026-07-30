package eu.vitamo.app.features.media.model

expect class PickedImage {

    val fileName: String
    val mimeType: String

    suspend fun readBytes(
        maxBytes: Long,
    ): ByteArray

    suspend fun cleanup()
}