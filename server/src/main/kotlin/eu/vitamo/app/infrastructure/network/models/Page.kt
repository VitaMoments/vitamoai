package eu.vitamo.app.infrastructure.network.models

import eu.vitamo.app.api.result.PagedResult

data class Page<T>(
    val items: List<T>,
    val limit: Int,
    val offset: Long,
    val total: Long,
)

fun <T, R> Page<T>.toPagedResult(
    mappedItems: List<R>,
): PagedResult<R> {
    require(mappedItems.size == items.size) {
        "Mapped items must have the same size as the original page"
    }

    val nextOffsetValue = offset + items.size
    val hasMore = nextOffsetValue < total

    return PagedResult(
        items = mappedItems,
        limit = limit,
        offset = offset,
        total = total,
        hasMore = hasMore,
        nextOffset = nextOffsetValue.takeIf { hasMore },
    )
}

inline fun <T, R> Page<T>.toPagedResult(
    transform: (T) -> R,
): PagedResult<R> =
    toPagedResult(
        mappedItems = items.map(transform),
    )