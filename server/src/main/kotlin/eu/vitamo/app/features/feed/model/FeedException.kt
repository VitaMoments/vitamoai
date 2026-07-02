package eu.vitamo.app.features.feed.model

import eu.vitamo.app.api.contracts.feed.FeedErrorCode
import io.ktor.http.HttpStatusCode

sealed class FeedException(
    open val code: String,
    override val message: String,
    open val status: HttpStatusCode,
) : RuntimeException(message) {
    data class InvalidContent(
        override val message: String,
        override val code: String = FeedErrorCode.INVALID_CONTENT_CODE,
    ) : FeedException(code = code, message = message, status = HttpStatusCode.BadRequest)

    data class InvalidCategories(
        override val message: String = "Categories contain invalid values or duplicates",
    ) : FeedException(
        code = FeedErrorCode.INVALID_CATEGORIES_CODE,
        message = message,
        status = HttpStatusCode.BadRequest,
    )

    data class InvalidMediaAssets(
        override val message: String = "One or more media assets are invalid",
    ) : FeedException(
        code = FeedErrorCode.INVALID_MEDIA_ASSETS_CODE,
        message = message,
        status = HttpStatusCode.BadRequest,
    )

    data class InvalidPagination(
        override val message: String = "Limit must be between 1 and 100 and offset must be >= 0",
    ) : FeedException(
        code = FeedErrorCode.INVALID_PAGINATION_CODE,
        message = message,
        status = HttpStatusCode.BadRequest,
    )

    data class Forbidden(
        override val message: String,
    ) : FeedException(
        code = FeedErrorCode.FORBIDDEN_CODE,
        message = message,
        status = HttpStatusCode.Forbidden,
    )

    data class NotFound(
        override val message: String = "Feed item not found",
    ) : FeedException(
        code = FeedErrorCode.NOT_FOUND_CODE,
        message = message,
        status = HttpStatusCode.NotFound,
    )
}
