package eu.vitamo.app.features.feed.platform

private class JvmFeedPlatform : FeedPlatform {
    override val supportsRichEditor: Boolean = true
}

actual fun feedPlatform(): FeedPlatform = JvmFeedPlatform()
