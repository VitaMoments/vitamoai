package eu.vitamo.app.features.media.processor

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import java.awt.image.BufferedImage
import java.nio.file.Path

object ExifOrientationNormalizer {

    fun normalize(
        source: Path,
        image: BufferedImage,
    ): BufferedImage {
        val orientation = readOrientation(source)

        return normalize(
            image = image,
            orientation = orientation,
        )
    }

    internal fun normalize(
        image: BufferedImage,
        orientation: Int,
    ): BufferedImage {
        if (orientation == ORIENTATION_NORMAL) {
            return image
        }

        val sourceWidth = image.width
        val sourceHeight = image.height

        val swapsDimensions = orientation in setOf(
            ORIENTATION_TRANSPOSE,
            ORIENTATION_ROTATE_90,
            ORIENTATION_TRANSVERSE,
            ORIENTATION_ROTATE_270,
        )

        val targetWidth = if (swapsDimensions) {
            sourceHeight
        } else {
            sourceWidth
        }

        val targetHeight = if (swapsDimensions) {
            sourceWidth
        } else {
            sourceHeight
        }

        val imageType = if (image.colorModel.hasAlpha()) {
            BufferedImage.TYPE_INT_ARGB
        } else {
            BufferedImage.TYPE_INT_RGB
        }

        val sourcePixels = image.getRGB(
            0,
            0,
            sourceWidth,
            sourceHeight,
            null,
            0,
            sourceWidth,
        )

        val targetPixels = IntArray(
            targetWidth * targetHeight,
        )

        for (sourceY in 0 until sourceHeight) {
            for (sourceX in 0 until sourceWidth) {
                val sourceIndex =
                    sourceY * sourceWidth + sourceX

                val targetCoordinates = targetCoordinates(
                    sourceX = sourceX,
                    sourceY = sourceY,
                    sourceWidth = sourceWidth,
                    sourceHeight = sourceHeight,
                    orientation = orientation,
                )

                val targetIndex =
                    targetCoordinates.y * targetWidth +
                            targetCoordinates.x

                targetPixels[targetIndex] =
                    sourcePixels[sourceIndex]
            }
        }

        return BufferedImage(
            targetWidth,
            targetHeight,
            imageType,
        ).apply {
            setRGB(
                0,
                0,
                targetWidth,
                targetHeight,
                targetPixels,
                0,
                targetWidth,
            )
        }
    }

    private fun readOrientation(
        source: Path,
    ): Int {
        return runCatching {
            val metadata =
                ImageMetadataReader.readMetadata(
                    source.toFile(),
                )

            val directory =
                metadata.getFirstDirectoryOfType(
                    ExifIFD0Directory::class.java,
                )

            directory?.getInteger(
                ExifIFD0Directory.TAG_ORIENTATION,
            )
        }.getOrNull()
            ?.takeIf { it in MIN_ORIENTATION..MAX_ORIENTATION }
            ?: ORIENTATION_NORMAL
    }

    private fun targetCoordinates(
        sourceX: Int,
        sourceY: Int,
        sourceWidth: Int,
        sourceHeight: Int,
        orientation: Int,
    ): Coordinates {
        return when (orientation) {
            ORIENTATION_MIRROR_HORIZONTAL -> {
                Coordinates(
                    x = sourceWidth - 1 - sourceX,
                    y = sourceY,
                )
            }

            ORIENTATION_ROTATE_180 -> {
                Coordinates(
                    x = sourceWidth - 1 - sourceX,
                    y = sourceHeight - 1 - sourceY,
                )
            }

            ORIENTATION_MIRROR_VERTICAL -> {
                Coordinates(
                    x = sourceX,
                    y = sourceHeight - 1 - sourceY,
                )
            }

            ORIENTATION_TRANSPOSE -> {
                Coordinates(
                    x = sourceY,
                    y = sourceX,
                )
            }

            ORIENTATION_ROTATE_90 -> {
                Coordinates(
                    x = sourceHeight - 1 - sourceY,
                    y = sourceX,
                )
            }

            ORIENTATION_TRANSVERSE -> {
                Coordinates(
                    x = sourceHeight - 1 - sourceY,
                    y = sourceWidth - 1 - sourceX,
                )
            }

            ORIENTATION_ROTATE_270 -> {
                Coordinates(
                    x = sourceY,
                    y = sourceWidth - 1 - sourceX,
                )
            }

            else -> {
                Coordinates(
                    x = sourceX,
                    y = sourceY,
                )
            }
        }
    }

    private data class Coordinates(
        val x: Int,
        val y: Int,
    )

    private const val MIN_ORIENTATION = 1
    private const val MAX_ORIENTATION = 8

    private const val ORIENTATION_NORMAL = 1
    private const val ORIENTATION_MIRROR_HORIZONTAL = 2
    private const val ORIENTATION_ROTATE_180 = 3
    private const val ORIENTATION_MIRROR_VERTICAL = 4
    private const val ORIENTATION_TRANSPOSE = 5
    private const val ORIENTATION_ROTATE_90 = 6
    private const val ORIENTATION_TRANSVERSE = 7
    private const val ORIENTATION_ROTATE_270 = 8
}