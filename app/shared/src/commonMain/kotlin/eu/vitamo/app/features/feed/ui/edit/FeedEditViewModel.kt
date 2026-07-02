package eu.vitamo.app.features.feed.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.contracts.feed.RichTextDocument
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.uuid.Uuid

data class FeedEditState(
    val content: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

class FeedEditViewModel(
    private val feedRepository: FeedRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedEditState())
    val state: StateFlow<FeedEditState> = _state.asStateFlow()

    fun onContentChanged(value: String) {
        _state.update { it.copy(content = value, error = null, saved = false) }
    }

    fun save(uuid: Uuid) {
        val content = state.value.content.trim()
        if (content.isBlank()) {
            _state.update { it.copy(error = "Content is required") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (
                feedRepository.updateFeedItem(
                    uuid = uuid,
                    request = UpdateFeedItemRequest(
                        content = RichTextDocument(
                            type = "markdown",
                            content = buildJsonObject { put("text", content) },
                        ),
                    ),
                )
            ) {
                is RepositoryResult.Success -> _state.update { it.copy(isLoading = false, saved = true) }
                is RepositoryResult.Error -> _state.update { it.copy(isLoading = false, error = "Opslaan mislukt") }
            }
        }
    }
}
