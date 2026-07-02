package eu.vitamo.app.features.feed.validation

import eu.vitamo.app.features.feed.model.FeedException
import eu.vitamo.app.api.contracts.feed.CreateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.RichTextDocument

class FeedInputValidator {
    fun validateCreate(request: CreateFeedItemRequest) {
        validateContent(request.content)
        validateCategories(request.categories)
        validateMediaAssets(request.mediaAssets)
    }

    fun validateUpdate(request: UpdateFeedItemRequest) {
        request.content?.let(::validateContent)
        request.categories?.let(::validateCategories)
        request.mediaAssets?.let(::validateMediaAssets)
    }

    private fun validateContent(content: RichTextDocument) {
        val type = content.type.trim().lowercase()
        if (type !in setOf("markdown", "plaintext", "document", "html")) {
            throw FeedException.InvalidContent("Content type must be one of markdown, plaintext, document, html")
        }

        if (content.content.toString() == "{}") {
            throw FeedException.InvalidContent("Content cannot be empty")
        }
        val textCandidate = content.content.toString()
        if (textCandidate.contains("\"text\":\"\"")) {
            throw FeedException.InvalidContent("Content cannot be empty")
        }

        val serialized = textCandidate
        if (serialized.length > 10_000_000) {
            throw FeedException.InvalidContent("Content too large")
        }
        if (serialized.length > 500_000) {
            throw FeedException.InvalidContent("Content exceeds character limit")
        }
    }

    private fun validateCategories(categories: List<eu.vitamo.app.api.contracts.feed.FeedCategory>) {
        if (categories.distinct().size != categories.size || categories.size > 13) {
            throw FeedException.InvalidCategories()
        }
    }

    private fun validateMediaAssets(mediaAssets: List<eu.vitamo.app.api.contracts.feed.MediaAsset>) {
        if (mediaAssets.distinctBy { it.id }.size != mediaAssets.size) {
            throw FeedException.InvalidMediaAssets("Media assets contain duplicates")
        }
        mediaAssets.forEach { media ->
            if (!(media.url.startsWith("http://") || media.url.startsWith("https://"))) {
                throw FeedException.InvalidMediaAssets("Media asset URLs must use http/https")
            }
        }
    }
}
