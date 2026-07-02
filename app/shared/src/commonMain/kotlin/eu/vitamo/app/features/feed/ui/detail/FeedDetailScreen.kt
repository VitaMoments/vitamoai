package eu.vitamo.app.features.feed.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Feed item detail", style = MaterialTheme.typography.headlineSmall)
        Text("ID: $feedItemId")
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
