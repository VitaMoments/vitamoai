package eu.vitamo.app.features.feed.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.contracts.feed.CreateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.PrivacyStatus
import eu.vitamo.app.api.contracts.feed.RichTextDocument
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class FeedCreateState(
    val content: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val created: Boolean = false,
)

class FeedCreateViewModel(
    private val feedRepository: FeedRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedCreateState())
    val state: StateFlow<FeedCreateState> = _state.asStateFlow()

    fun onContentChanged(value: String) {
        _state.update { it.copy(content = value, error = null, created = false) }
    }

    fun submit() {
        val content = state.value.content.trim()
        if (content.isBlank()) {
            _state.update { it.copy(error = "Content is required") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = feedRepository.createFeedItem(
                CreateFeedItemRequest(
                    content = RichTextDocument(
                        type = "markdown",
                        content = buildJsonObject { put("text", content) },
                    ),
                    privacy = PrivacyStatus.PUBLIC,
                )
            )
            when (result) {
                is RepositoryResult.Success -> _state.update { it.copy(isLoading = false, created = true) }
                is RepositoryResult.Error -> _state.update { it.copy(isLoading = false, error = result.error.message) }
            }
        }
    }
}
