package eu.vitamo.app.features.feed.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

data class FeedListState(
    val items: List<FeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val limit: Int = 20,
    val offset: Int = 0,
    val hasMore: Boolean = false,
)

class FeedListViewModel(
    private val feedRepository: FeedRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedListState())
    val state: StateFlow<FeedListState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = feedRepository.getGeneralFeed(limit = state.value.limit, offset = state.value.offset)) {
                is RepositoryResult.Success -> {
                    _state.update {
                        it.copy(
                            items = result.data.items,
                            hasMore = result.data.hasMore,
                            isLoading = false,
                        )
                    }
                }

                is RepositoryResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.error.message) }
                }
            }
        }
    }

    fun loadNextPage() {
        _state.update { it.copy(offset = it.offset + it.limit) }
        load()
    }

    fun loadPreviousPage() {
        _state.update { it.copy(offset = (it.offset - it.limit).coerceAtLeast(0)) }
        load()
    }

    fun deleteItem(uuid: Uuid) {
        viewModelScope.launch {
            when (feedRepository.deleteFeedItem(uuid)) {
                is RepositoryResult.Success -> load()
                is RepositoryResult.Error -> _state.update { it.copy(error = "Verwijderen mislukt") }
            }
        }
    }
}
