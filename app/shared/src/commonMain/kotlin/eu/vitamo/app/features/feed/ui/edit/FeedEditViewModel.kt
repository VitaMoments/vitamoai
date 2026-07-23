package eu.vitamo.app.features.feed.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.vitamo.app.api.contracts.feed.FeedCategory
import eu.vitamo.app.api.contracts.common.PrivacyStatus
import eu.vitamo.app.api.contracts.common.RichTextDocument
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.uuid.Uuid

data class FeedEditState(
    val content: String = "",
    val privacy: PrivacyStatus = PrivacyStatus.PUBLIC,
    val selectedCategories: List<FeedCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

class FeedEditViewModel(
    private val feedRepository: FeedRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(FeedEditState())
    val state: StateFlow<FeedEditState> = _state.asStateFlow()

    fun loadItem(uuid: Uuid) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = feedRepository.getFeedItem(uuid)) {
                is RepositoryResult.Success -> {
                    val item = result.data
                    val textContent = item.content?.content
                        ?.jsonObject?.get("text")
                        ?.toString()?.trim('"')
                        ?: ""
                    _state.update {
                        it.copy(
                            content = textContent,
                            privacy = item.privacy,
                            selectedCategories = item.categories,
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

    fun onContentChanged(value: String) {
        _state.update { it.copy(content = value, error = null, saved = false) }
    }

    fun onPrivacyChanged(value: PrivacyStatus) {
        _state.update { it.copy(privacy = value, error = null, saved = false) }
    }

    fun onCategoryToggled(category: FeedCategory) {
        _state.update { current ->
            val updated = if (current.selectedCategories.contains(category)) {
                current.selectedCategories - category
            } else {
                current.selectedCategories + category
            }
            current.copy(selectedCategories = updated, error = null, saved = false)
        }
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
                        privacy = state.value.privacy,
                        categories = state.value.selectedCategories,
                    ),
                )
            ) {
                is RepositoryResult.Success -> _state.update { it.copy(isLoading = false, saved = true) }
                is RepositoryResult.Error -> _state.update { it.copy(isLoading = false, error = "Opslaan mislukt") }
            }
        }
    }
}
