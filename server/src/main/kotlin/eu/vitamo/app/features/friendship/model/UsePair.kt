package eu.vitamo.app.features.friendship.model

import kotlin.uuid.Uuid

data class UserPair(
    val low: Uuid,
    val high: Uuid,
)

fun canonicalUserPair(
    first: Uuid,
    second: Uuid,
): UserPair {
    require(first != second) {
        "A friendship requires two different users."
    }

    return if (first.toString() < second.toString()) {
        UserPair(
            low = first,
            high = second,
        )
    } else {
        UserPair(
            low = second,
            high = first,
        )
    }
}