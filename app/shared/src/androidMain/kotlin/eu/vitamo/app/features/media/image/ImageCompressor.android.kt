package eu.vitamo.app.features.media.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import eu.vitamo.app.features.media.model.PickedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap

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
                    maxBytes = MAX_SOURCE_IMAGE_BYTES,
                )

            val decodedBitmap =
                decodeBitmap(
                    bytes = sourceBytes,
                    maxDimension = maxDimension,
                )

            try {
                val resizedBitmap =
                    resizeBitmap(
                        bitmap = decodedBitmap,
                        maxDimension = maxDimension,
                    )

                try {
                    val outputBitmap =
                        createOpaqueBitmapIfNeeded(
                            bitmap = resizedBitmap,
                        )

                    try {
                        val compressedBytes =
                            compressToTargetSize(
                                bitmap = outputBitmap,
                                initialQuality = quality,
                                maxBytes = maxBytes,
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
                    } finally {
                        if (
                            outputBitmap !== resizedBitmap
                        ) {
                            outputBitmap.recycle()
                        }
                    }
                } finally {
                    if (
                        resizedBitmap !== decodedBitmap
                    ) {
                        resizedBitmap.recycle()
                    }
                }
            } finally {
                decodedBitmap.recycle()
            }
        }

    private fun decodeBitmap(
        bytes: ByteArray,
        maxDimension: Int,
    ): Bitmap {
        val boundsOptions =
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

        BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            boundsOptions,
        )

        val width =
            boundsOptions.outWidth

        val height =
            boundsOptions.outHeight

        require(
            width > 0 &&
                height > 0,
        ) {
            "Het afbeeldingsformaat wordt niet ondersteund."
        }

        val decodeOptions =
            BitmapFactory.Options().apply {
                inSampleSize =
                    calculateInSampleSize(
                        width = width,
                        height = height,
                        maxDimension =
                            maxDimension,
                    )

                inJustDecodeBounds = false
            }

        return BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
            decodeOptions,
        ) ?: throw IllegalArgumentException(
            "De afbeelding kon niet worden gedecodeerd.",
        )
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        maxDimension: Int,
    ): Int {
        var sampleSize = 1

        /*
         * Decode niet meteen naar exact maxDimension.
         * We houden maximaal ongeveer 2x de gewenste
         * resolutie over zodat de uiteindelijke resize
         * nog voldoende kwaliteit heeft.
         */
        val decodeTarget =
            maxDimension * 2

        while (
            width / (sampleSize * 2) >=
                decodeTarget ||
            height / (sampleSize * 2) >=
                decodeTarget
        ) {
            sampleSize *= 2
        }

        return sampleSize
    }

    private fun resizeBitmap(
        bitmap: Bitmap,
        maxDimension: Int,
    ): Bitmap {
        val width =
            bitmap.width

        val height =
            bitmap.height

        if (
            width <= maxDimension &&
            height <= maxDimension
        ) {
            return bitmap
        }

        val scale =
            maxDimension.toFloat() /
                maxOf(
                    width,
                    height,
                )

        val targetWidth =
            (width * scale)
                .roundToInt()
                .coerceAtLeast(1)

        val targetHeight =
            (height * scale)
                .roundToInt()
                .coerceAtLeast(1)

        return bitmap.scale(targetWidth, targetHeight)
    }

    /**
     * JPEG heeft geen alpha channel.
     *
     * Transparante PNG/WebP-afbeeldingen krijgen
     * daarom een witte achtergrond in plaats van
     * bijvoorbeeld zwart.
     */
    private fun createOpaqueBitmapIfNeeded(
        bitmap: Bitmap,
    ): Bitmap {
        if (!bitmap.hasAlpha()) {
            return bitmap
        }

        return createBitmap(bitmap.width, bitmap.height).also { output ->
            Canvas(output).apply {
                drawColor(Color.WHITE)
                drawBitmap(
                    bitmap,
                    0f,
                    0f,
                    null,
                )
            }
        }
    }

    private fun compressToTargetSize(
        bitmap: Bitmap,
        initialQuality: Int,
        maxBytes: Long,
    ): ByteArray {
        var currentQuality =
            initialQuality

        while (
            currentQuality >=
            MIN_JPEG_QUALITY
        ) {
            val output =
                ByteArrayOutputStream()

            val success =
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    currentQuality,
                    output,
                )

            check(success) {
                "De afbeelding kon niet als JPEG worden opgeslagen."
            }

            val bytes =
                output.toByteArray()

            if (
                bytes.size.toLong() <=
                maxBytes
            ) {
                return bytes
            }

            currentQuality -=
                QUALITY_STEP
        }

        throw IllegalArgumentException(
            "De afbeelding kon niet voldoende worden verkleind.",
        )
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