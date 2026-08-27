package eu.vitamo.app.features.feed.routes

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal data class MultipartUploadLogEntry(
    val name: String?,
    val type: String?,
    val size: Long,
)

internal fun ApplicationCall.logMultipartUpload(files: List<MultipartUploadLogEntry>) {
    if (files.isEmpty()) return

    val payload = buildJsonObject {
        put("count", files.size)
        put("files", buildJsonArray {
            files.forEach { file ->
                add(
                    buildJsonObject {
                        put("name", file.name)
                        put("type", file.type)
                        put("size", file.size)
                    }
                )
            }
        })
    }

    application.log.info(
        "Received upload: ${Json.encodeToString(payload)}"
    )
}
