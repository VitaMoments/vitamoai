package eu.vitamo.app.features.feed.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.features.feed.repository.FeedRepository
import eu.vitamo.app.repository.RepositoryResult
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid
import org.koin.compose.koinInject

@Composable
fun FeedDetailScreen(
    feedItemId: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
) {
    val feedRepository: FeedRepository = koinInject()
    val coroutineScope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var item by remember { mutableStateOf<FeedItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var fetchError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(feedItemId) {
        val uuid = Uuid.parse(feedItemId)
        when (val result = feedRepository.getFeedItem(uuid)) {
            is RepositoryResult.Success -> {
                item = result.data
                isLoading = false
            }
            is RepositoryResult.Error -> {
                fetchError = result.error.message
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Feed item detail", style = MaterialTheme.typography.headlineSmall)

        when {
            isLoading -> {
                Text("Laden...", style = MaterialTheme.typography.bodyMedium)
            }
            fetchError != null -> {
                Text(fetchError!!, color = MaterialTheme.colorScheme.error)
            }
            item != null -> {
                val feedItem = item!!
                Text("Auteur: ${feedItem.author.displayName}")
                Text("Privacy: ${feedItem.privacy.name}")

                feedItem.content?.let { richText ->
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = richText.content?.toString() ?: "",
                        onValueChange = {},
                        label = { Text("Inhoud") },
                        readOnly = true,
                    )
                }

                if (feedItem.categories.isNotEmpty()) {
                    Text("Categorieën: ${feedItem.categories.joinToString { it.name }}")
                }

                Text("Aangemaakt: ${feedItem.createdAt}")
                Text("Bijgewerkt: ${feedItem.updatedAt}")
            }
        }

        actionError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(onClick = { pendingDelete = true }) {
            Text("Verwijder")
        }
        Button(onClick = onBack) {
            Text("Terug")
        }
    }

    if (pendingDelete) {
        AlertDialog(
            onDismissRequest = { pendingDelete = false },
            title = { Text("Item verwijderen?") },
            text = { Text("Na verwijderen wordt het item niet meer getoond in de feed.") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDelete = false
                        coroutineScope.launch {
                            val result = runCatching { Uuid.parse(feedItemId) }
                                .getOrNull()
                                ?.let { feedRepository.deleteFeedItem(it) }
                            if (result is RepositoryResult.Success) {
                                onDeleted()
                            } else {
                                actionError = "Verwijderen mislukt"
                            }
                        }
                    },
                ) {
                    Text("Verwijder")
                }
            },
            dismissButton = {
                Button(onClick = { pendingDelete = false }) {
                    Text("Annuleer")
                }
            },
        )
    }
}
