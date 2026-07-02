package eu.vitamo.app.features.feed.platform

interface FeedPlatform {
    val supportsRichEditor: Boolean
}

expect fun feedPlatform(): FeedPlatform
