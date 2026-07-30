package eu.vitamo.app.features.media.processor

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProcessedImage(
    val path: Path,
    val extension: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val sha256: String,
)

class InvalidImageException(
    override val message: String,
) : RuntimeException(message)

class ImageProcessor(
    private val maxInputBytes: Long = 10L * 1024L * 1024L,
    private val maxWidth: Int = 8_000,
    private val maxHeight: Int = 8_000,
    private val maxPixels: Long = 25_000_000L,
    private val jpegQuality: Float = 0.88f,
) {

    init {
        require(maxInputBytes > 0L) {
            "maxInputBytes must be greater than zero."
        }

        require(maxWidth > 0) {
            "maxWidth must be greater than zero."
        }

        require(maxHeight > 0) {
            "maxHeight must be greater than zero."
        }

        require(maxPixels > 0L) {
            "maxPixels must be greater than zero."
        }

        require(jpegQuality in 0f..1f) {
            "jpegQuality must be between 0.0 and 1.0."
        }
    }

    suspend fun process(
        source: Path,
    ): ProcessedImage = withContext(Dispatchers.IO) {
        validateFileSize(
            source = source,
        )

        val exifOrientation = readExifOrientation(
            source = source,
        )

        val imageInput = ImageIO.createImageInputStream(
            source.toFile(),
        ) ?: throw InvalidImageException(
            message = "Het bestand kon niet als afbeelding worden gelezen.",
        )

        imageInput.use { input ->
            val readers = ImageIO.getImageReaders(input)

            if (!readers.hasNext()) {
                throw InvalidImageException(
                    message = "Het bestand is geen geldige JPEG- of PNG-afbeelding.",
                )
            }

            val reader = readers.next()

            try {
                reader.input = input

                val format = reader.formatName
                    .lowercase()

                if (format !in ALLOWED_INPUT_FORMATS) {
                    throw InvalidImageException(
                        message = "Alleen JPEG- en PNG-afbeeldingen zijn toegestaan.",
                    )
                }

                /*
                 * Controleer de afmetingen voordat de volledige afbeelding
                 * wordt gedecodeerd. Dit beschermt tegen extreem grote
                 * afbeeldingen.
                 */
                val sourceWidth = reader.getWidth(0)
                val sourceHeight = reader.getHeight(0)

                validateDimensions(
                    width = sourceWidth,
                    height = sourceHeight,
                )

                val decodedImage = reader.read(0)
                    ?: throw InvalidImageException(
                        message = "De afbeelding kon niet worden gedecodeerd.",
                    )

                /*
                 * ImageIO draait camerafoto's niet automatisch aan de hand
                 * van de EXIF-orientation. Daarom worden de pixels hier
                 * definitief in de juiste stand gezet.
                 */
                val orientedImage = applyExifOrientation(
                    image = decodedImage,
                    orientation = exifOrientation,
                )

                validateDimensions(
                    width = orientedImage.width,
                    height = orientedImage.height,
                )

                val outputFormat = determineOutputFormat(
                    image = orientedImage,
                )

                /*
                 * Zorg voor een standaard RGB-afbeelding wanneer de uitvoer
                 * JPEG is. Dit voorkomt problemen met afwijkende color models.
                 */
                val outputImage = when (outputFormat) {
                    OutputFormat.JPEG -> {
                        convertToRgb(
                            image = orientedImage,
                        )
                    }

                    OutputFormat.PNG -> {
                        orientedImage
                    }
                }

                val outputPath = Files.createTempFile(
                    "vitamo-processed-image-",
                    ".${outputFormat.extension}",
                )

                try {
                    when (outputFormat) {
                        OutputFormat.PNG -> {
                            writePng(
                                image = outputImage,
                                target = outputPath,
                            )
                        }

                        OutputFormat.JPEG -> {
                            writeJpeg(
                                image = outputImage,
                                target = outputPath,
                            )
                        }
                    }

                    ProcessedImage(
                        path = outputPath,
                        extension = outputFormat.extension,
                        mimeType = outputFormat.mimeType,
                        sizeBytes = Files.size(outputPath),
                        width = outputImage.width,
                        height = outputImage.height,
                        sha256 = calculateSha256(
                            path = outputPath,
                        ),
                    )
                } catch (cause: Throwable) {
                    Files.deleteIfExists(outputPath)
                    throw cause
                }
            } finally {
                reader.dispose()
            }
        }
    }

    /**
     * Leest de EXIF-orientation.
     *
     * JPEG-camera-afbeeldingen bevatten vaak ongedraaide pixels met alleen
     * een metadatawaarde die aangeeft hoe de afbeelding getoond moet worden.
     *
     * Bij ontbrekende of ongeldige metadata wordt orientation 1 gebruikt.
     */
    private fun readExifOrientation(
        source: Path,
    ): Int {
        return runCatching {
            val metadata = ImageMetadataReader.readMetadata(
                source.toFile(),
            )

            metadata
                .getFirstDirectoryOfType(
                    ExifIFD0Directory::class.java,
                )
                ?.getInteger(
                    ExifIFD0Directory.TAG_ORIENTATION,
                )
        }
            .getOrNull()
            ?.takeIf { orientation ->
                orientation in MIN_EXIF_ORIENTATION..MAX_EXIF_ORIENTATION
            }
            ?: EXIF_ORIENTATION_NORMAL
    }

    /**
     * Past alle acht mogelijke EXIF-orientaties toe.
     *
     * Oriëntaties 5 t/m 8 verwisselen breedte en hoogte.
     */
    private fun applyExifOrientation(
        image: BufferedImage,
        orientation: Int,
    ): BufferedImage {
        if (orientation == EXIF_ORIENTATION_NORMAL) {
            return image
        }

        val sourceWidth = image.width
        val sourceHeight = image.height

        val dimensionsAreSwapped = orientation in setOf(
            EXIF_ORIENTATION_TRANSPOSE,
            EXIF_ORIENTATION_ROTATE_90,
            EXIF_ORIENTATION_TRANSVERSE,
            EXIF_ORIENTATION_ROTATE_270,
        )

        val targetWidth = if (dimensionsAreSwapped) {
            sourceHeight
        } else {
            sourceWidth
        }

        val targetHeight = if (dimensionsAreSwapped) {
            sourceWidth
        } else {
            sourceHeight
        }

        val targetType = if (image.colorModel.hasAlpha()) {
            BufferedImage.TYPE_INT_ARGB
        } else {
            BufferedImage.TYPE_INT_RGB
        }

        val transformedImage = BufferedImage(
            targetWidth,
            targetHeight,
            targetType,
        )

        val transform = createExifTransform(
            orientation = orientation,
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
        )

        transformedImage
            .createGraphics()
            .use { graphics ->
                graphics.drawImage(
                    image,
                    transform,
                    null,
                )
            }

        return transformedImage
    }

    private fun createExifTransform(
        orientation: Int,
        sourceWidth: Int,
        sourceHeight: Int,
    ): AffineTransform {
        val width = sourceWidth.toDouble()
        val height = sourceHeight.toDouble()

        return when (orientation) {
            /*
             * Horizontaal gespiegeld.
             */
            EXIF_ORIENTATION_MIRROR_HORIZONTAL -> {
                AffineTransform(
                    -1.0,
                    0.0,
                    0.0,
                    1.0,
                    width,
                    0.0,
                )
            }

            /*
             * 180 graden gedraaid.
             */
            EXIF_ORIENTATION_ROTATE_180 -> {
                AffineTransform(
                    -1.0,
                    0.0,
                    0.0,
                    -1.0,
                    width,
                    height,
                )
            }

            /*
             * Verticaal gespiegeld.
             */
            EXIF_ORIENTATION_MIRROR_VERTICAL -> {
                AffineTransform(
                    1.0,
                    0.0,
                    0.0,
                    -1.0,
                    0.0,
                    height,
                )
            }

            /*
             * Gespiegeld over de diagonaal linksboven-rechtsonder.
             */
            EXIF_ORIENTATION_TRANSPOSE -> {
                AffineTransform(
                    0.0,
                    1.0,
                    1.0,
                    0.0,
                    0.0,
                    0.0,
                )
            }

            /*
             * 90 graden met de klok mee.
             */
            EXIF_ORIENTATION_ROTATE_90 -> {
                AffineTransform(
                    0.0,
                    1.0,
                    -1.0,
                    0.0,
                    height,
                    0.0,
                )
            }

            /*
             * Gespiegeld over de diagonaal rechtsboven-linksonder.
             */
            EXIF_ORIENTATION_TRANSVERSE -> {
                AffineTransform(
                    0.0,
                    -1.0,
                    -1.0,
                    0.0,
                    height,
                    width,
                )
            }

            /*
             * 270 graden met de klok mee, oftewel 90 graden tegen
             * de klok in.
             */
            EXIF_ORIENTATION_ROTATE_270 -> {
                AffineTransform(
                    0.0,
                    -1.0,
                    1.0,
                    0.0,
                    0.0,
                    width,
                )
            }

            else -> {
                AffineTransform()
            }
        }
    }

    private fun validateFileSize(
        source: Path,
    ) {
        if (!Files.isRegularFile(source)) {
            throw InvalidImageException(
                message = "Het geüploade bestand bestaat niet.",
            )
        }

        val size = Files.size(source)

        if (size <= 0L) {
            throw InvalidImageException(
                message = "De afbeelding is leeg.",
            )
        }

        if (size > maxInputBytes) {
            throw InvalidImageException(
                message = "De afbeelding mag maximaal 10 MB groot zijn.",
            )
        }
    }

    private fun validateDimensions(
        width: Int,
        height: Int,
    ) {
        if (width <= 0 || height <= 0) {
            throw InvalidImageException(
                message = "De afbeelding heeft ongeldige afmetingen.",
            )
        }

        if (width > maxWidth || height > maxHeight) {
            throw InvalidImageException(
                message = "De afbeelding heeft een te hoge resolutie.",
            )
        }

        val pixels = width.toLong() * height.toLong()

        if (pixels > maxPixels) {
            throw InvalidImageException(
                message = "De afbeelding bevat te veel pixels.",
            )
        }
    }

    private fun determineOutputFormat(
        image: BufferedImage,
    ): OutputFormat {
        return if (image.colorModel.hasAlpha()) {
            OutputFormat.PNG
        } else {
            OutputFormat.JPEG
        }
    }

    /**
     * Zet afbeeldingen met een afwijkend color model om naar standaard RGB.
     *
     * Dit voorkomt onder andere JPEG-writerfouten bij sommige camera- en
     * galerijafbeeldingen.
     */
    private fun convertToRgb(
        image: BufferedImage,
    ): BufferedImage {
        if (
            image.type == BufferedImage.TYPE_INT_RGB &&
            !image.colorModel.hasAlpha()
        ) {
            return image
        }

        return BufferedImage(
            image.width,
            image.height,
            BufferedImage.TYPE_INT_RGB,
        ).apply {
            createGraphics().use { graphics ->
                graphics.drawImage(
                    image,
                    0,
                    0,
                    null,
                )
            }
        }
    }

    private fun writePng(
        image: BufferedImage,
        target: Path,
    ) {
        val written = ImageIO.write(
            image,
            "png",
            target.toFile(),
        )

        if (!written) {
            throw InvalidImageException(
                message = "De PNG-afbeelding kon niet worden opgeslagen.",
            )
        }
    }

    private fun writeJpeg(
        image: BufferedImage,
        target: Path,
    ) {
        val writers = ImageIO.getImageWritersByFormatName(
            "jpeg",
        )

        if (!writers.hasNext()) {
            throw IllegalStateException(
                "No JPEG writer is available.",
            )
        }

        val writer = writers.next()

        try {
            Files.newOutputStream(target).use { outputStream ->
                ImageIO.createImageOutputStream(
                    outputStream,
                ).use { output ->
                    writer.output = output

                    val parameters = writer.defaultWriteParam

                    if (parameters.canWriteCompressed()) {
                        parameters.compressionMode =
                            ImageWriteParam.MODE_EXPLICIT

                        parameters.compressionQuality =
                            jpegQuality
                    }

                    writer.write(
                        null,
                        IIOImage(
                            image,
                            null,
                            null,
                        ),
                        parameters,
                    )
                }
            }
        } finally {
            writer.dispose()
        }
    }

    private fun calculateSha256(
        path: Path,
    ): String {
        val digest = MessageDigest.getInstance(
            "SHA-256",
        )

        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

            while (true) {
                val read = input.read(buffer)

                if (read < 0) {
                    break
                }

                digest.update(
                    buffer,
                    0,
                    read,
                )
            }
        }

        return digest
            .digest()
            .joinToString(separator = "") { byte ->
                "%02x".format(byte)
            }
    }

    private enum class OutputFormat(
        val extension: String,
        val mimeType: String,
    ) {
        JPEG(
            extension = "jpg",
            mimeType = "image/jpeg",
        ),

        PNG(
            extension = "png",
            mimeType = "image/png",
        ),
    }

    private companion object {
        val ALLOWED_INPUT_FORMATS = setOf(
            "jpeg",
            "jpg",
            "png",
        )

        const val MIN_EXIF_ORIENTATION = 1
        const val MAX_EXIF_ORIENTATION = 8

        const val EXIF_ORIENTATION_NORMAL = 1
        const val EXIF_ORIENTATION_MIRROR_HORIZONTAL = 2
        const val EXIF_ORIENTATION_ROTATE_180 = 3
        const val EXIF_ORIENTATION_MIRROR_VERTICAL = 4
        const val EXIF_ORIENTATION_TRANSPOSE = 5
        const val EXIF_ORIENTATION_ROTATE_90 = 6
        const val EXIF_ORIENTATION_TRANSVERSE = 7
        const val EXIF_ORIENTATION_ROTATE_270 = 8
    }
}

private inline fun Graphics2D.use(
    block: (Graphics2D) -> Unit,
) {
    try {
        block(this)
    } finally {
        dispose()
    }
}