@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
)

package eu.vitamo.app.features.media.image

import eu.vitamo.app.features.media.model.PickedImage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.posix.memcpy
import kotlinx.cinterop.useContents

actual class ImageCompressor {

    actual suspend fun compress(
        image: PickedImage,
        maxDimension: Int,
        quality: Int,
        maxBytes: Long,
    ): CompressedImage =
        withContext(Dispatchers.Default) {

            require(maxDimension > 0) {
                "maxDimension must be greater than zero."
            }

            require(quality in 1..100) {
                "quality must be between 1 and 100."
            }

            require(maxBytes > 0L) {
                "maxBytes must be greater than zero."
            }

            val sourceBytes =
                image.readBytes(
                    maxBytes =
                        MAX_SOURCE_IMAGE_BYTES,
                )

            val sourceData =
                sourceBytes.toNSData()

            val sourceImage =
                UIImage(
                    data = sourceData,
                )

            requireNotNull(sourceImage) {
                "Het afbeeldingsformaat wordt niet ondersteund."
            }

            val resizedImage =
                resizeImage(
                    image = sourceImage,
                    maxDimension =
                        maxDimension,
                )

            val compressedBytes =
                compressToTargetSize(
                    image = resizedImage,
                    initialQuality =
                        quality,
                    maxBytes =
                        maxBytes,
                )

            CompressedImage(
                fileName =
                    image.fileName
                        .toJpegFileName(),
                mimeType =
                    JPEG_MIME_TYPE,
                bytes =
                    compressedBytes,
            )
        }

    private fun resizeImage(
        image: UIImage,
        maxDimension: Int,
    ): UIImage {
        val sourceWidth =
            image.size.useContents {
                width
            }

        val sourceHeight =
            image.size.useContents {
                height
            }

        if (
            sourceWidth <= maxDimension &&
            sourceHeight <= maxDimension
        ) {
            return image
        }

        val scale =
            maxDimension.toDouble() /
                maxOf(
                    sourceWidth,
                    sourceHeight,
                )

        val targetWidth =
            (sourceWidth * scale)
                .coerceAtLeast(1.0)

        val targetHeight =
            (sourceHeight * scale)
                .coerceAtLeast(1.0)

        /*
         * Scale = 1.0 is bewust.
         *
         * Bij 0.0 gebruikt UIKit de Retina screen scale
         * (2x/3x), waardoor een afbeelding van 1920px
         * ineens weer veel groter kan worden.
         */
        UIGraphicsBeginImageContextWithOptions(
            size =
                CGSizeMake(
                    targetWidth,
                    targetHeight,
                ),
            opaque = true,
            scale = 1.0,
        )

        return try {
            image.drawInRect(
                CGRectMake(
                    x = 0.0,
                    y = 0.0,
                    width = targetWidth,
                    height = targetHeight,
                ),
            )

            UIGraphicsGetImageFromCurrentImageContext()
                ?: throw IllegalArgumentException(
                    "De afbeelding kon niet worden verkleind.",
                )
        } finally {
            UIGraphicsEndImageContext()
        }
    }

    private fun compressToTargetSize(
        image: UIImage,
        initialQuality: Int,
        maxBytes: Long,
    ): ByteArray {
        var currentQuality =
            initialQuality

        while (
            currentQuality >=
            MIN_JPEG_QUALITY
        ) {
            val jpegData =
                UIImageJPEGRepresentation(
                    image = image,
                    compressionQuality =
                        currentQuality /
                            100.0,
                )
                    ?: throw IllegalArgumentException(
                        "De afbeelding kon niet als JPEG worden opgeslagen.",
                    )

            if (
                jpegData.length.toLong() <=
                maxBytes
            ) {
                return jpegData
                    .toByteArray()
            }

            currentQuality -=
                QUALITY_STEP
        }

        throw IllegalArgumentException(
            "De afbeelding kon niet voldoende worden verkleind.",
        )
    }
}

@OptIn(BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) {
        return NSData()
    }

    return usePinned { pinned ->
        NSData.create(
            bytes =
                pinned.addressOf(0),
            length =
                size.toULong(),
        )
    }
}

private fun NSData.toByteArray(): ByteArray {
    val byteCount =
        length.toInt()

    if (byteCount == 0) {
        return ByteArray(0)
    }

    return ByteArray(
        size = byteCount,
    ).also { destination ->

        destination.usePinned { pinned ->
            memcpy(
                pinned.addressOf(0),
                bytes,
                length,
            )
        }
    }
}

private fun String.toJpegFileName(): String {
    val baseName =
        substringBeforeLast(
            delimiter = ".",
            missingDelimiterValue = this,
        )
            .trim()
            .ifBlank {
                "image"
            }

    return "$baseName.jpg"
}

private const val JPEG_MIME_TYPE =
    "image/jpeg"

private const val MIN_JPEG_QUALITY =
    45

private const val QUALITY_STEP =
    7

private const val MAX_SOURCE_IMAGE_BYTES =
    40L * 1024L * 1024L