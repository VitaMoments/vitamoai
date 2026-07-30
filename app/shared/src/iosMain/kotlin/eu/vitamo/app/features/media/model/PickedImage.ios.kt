@file:OptIn(ExperimentalForeignApi::class)

package eu.vitamo.app.features.media.model

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.posix.memcpy

actual class PickedImage internal constructor(
    private val data: NSData,
    actual val fileName: String,
    actual val mimeType: String,
) {

    actual suspend fun readBytes(
        maxBytes: Long,
    ): ByteArray {
        require(maxBytes > 0L) {
            "maxBytes must be greater than zero."
        }

        val size = data.length.toLong()

        if (size <= 0L) {
            throw IllegalArgumentException(
                "De afbeelding is leeg.",
            )
        }

        if (size > maxBytes) {
            throw IllegalArgumentException(
                "De afbeelding is groter dan toegestaan.",
            )
        }

        return data.toByteArray()
    }

    actual suspend fun cleanup() {
        // NSData wordt automatisch door Kotlin/Native en iOS beheerd.
    }
}

private fun NSData.toByteArray(): ByteArray {
    val byteCount = length.toInt()

    if (byteCount == 0) {
        return ByteArray(0)
    }

    return ByteArray(byteCount).also { destination ->
        destination.usePinned { pinned ->
            memcpy(
                pinned.addressOf(0),
                bytes,
                length,
            )
        }
    }
}