package eu.vitamo.app.features.feed.platform

private class IosFeedPlatform : FeedPlatform {
    override val supportsRichEditor: Boolean = false
}

actual fun feedPlatform(): FeedPlatform = IosFeedPlatform()
