package eu.vitamo.app.features.feed.platform

private class AndroidFeedPlatform : FeedPlatform {
    override val supportsRichEditor: Boolean = true
}

actual fun feedPlatform(): FeedPlatform = AndroidFeedPlatform()
