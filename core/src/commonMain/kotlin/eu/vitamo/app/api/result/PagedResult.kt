package eu.vitamo.app.api.result

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class PagedResult<T>(
    val items: List<@Polymorphic T>,
    val limit: Int,
    val offset: Long,
    val total: Long,
    val hasMore: Boolean,
    val nextOffset: Long?
)