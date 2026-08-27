package eu.vitamo.app.ui.feed

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val feedUiKoinModule = module {
    single {
        CreatePostDraftStore()
    }

    viewModelOf(::FeedViewModel)
    viewModelOf(::CreatePostViewModel,)
}