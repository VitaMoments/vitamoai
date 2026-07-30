package eu.vitamo.app.features.media.model

import eu.vitamo.app.api.contracts.media.MediaReference

class MediaUrlResolver(
    private val apiBaseUrl: String,
) {
    fun resolve(
        media: MediaReference?,
    ): String? {
        val contentPath = media
            ?.contentPath
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?: return null

        if (
            contentPath.startsWith("http://") ||
            contentPath.startsWith("https://")
        ) {
            return contentPath
        }

        return apiBaseUrl.trimEnd('/') +
                "/" +
                contentPath.trimStart('/')
    }
}