package eu.vitamo.app.features.media.model

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class PickedImage internal constructor(
    context: Context,
    private val uri: Uri,
    actual val fileName: String,
    actual val mimeType: String,
    private val temporaryFile: File?,
) {

    private val applicationContext =
        context.applicationContext

    actual suspend fun readBytes(
        maxBytes: Long,
    ): ByteArray = withContext(Dispatchers.IO) {
        require(maxBytes > 0L) {
            "maxBytes must be greater than zero."
        }

        val inputStream = temporaryFile
            ?.takeIf(File::isFile)
            ?.inputStream()
            ?: applicationContext
                .contentResolver
                .openInputStream(uri)
            ?: error(
                "De geselecteerde afbeelding kon niet worden geopend.",
            )

        inputStream.use { input ->
            input.readBytesLimited(
                maxBytes = maxBytes,
            )
        }
    }

    actual suspend fun cleanup() {
        withContext(Dispatchers.IO) {
            temporaryFile?.delete()
        }
    }
}

private fun InputStream.readBytesLimited(
    maxBytes: Long,
): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

    var totalBytes = 0L

    while (true) {
        val bytesRead = read(buffer)

        if (bytesRead < 0) {
            break
        }

        totalBytes += bytesRead

        if (totalBytes > maxBytes) {
            throw IllegalArgumentException(
                "De afbeelding is groter dan toegestaan.",
            )
        }

        output.write(
            buffer,
            0,
            bytesRead,
        )
    }

    return output.toByteArray()
}