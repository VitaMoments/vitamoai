package eu.vitamo.app.features.feed.ui

import eu.vitamo.app.features.feed.ui.create.FeedCreateViewModel
import eu.vitamo.app.features.feed.ui.edit.FeedEditViewModel
import eu.vitamo.app.features.feed.ui.list.FeedListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val FeedUiKoinModule = module {
    viewModelOf(::FeedListViewModel)
    viewModelOf(::FeedCreateViewModel)
    viewModelOf(::FeedEditViewModel)
}
