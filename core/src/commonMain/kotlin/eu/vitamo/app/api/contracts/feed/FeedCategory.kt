package eu.vitamo.app.api.contracts.feed

import kotlinx.serialization.Serializable

@Serializable
enum class FeedCategory {
    MENTAL,
    PHYSICAL,
    FOOD,
    LIFESTYLE,
    MINDFULNESS,
    HABITS,
    SLEEP,
    ENERGY,
    RELATIONSHIPS,
    COMMUNITY,
    PURPOSE,
    PERSONAL_GROWTH,
    REFLECTION,
}
