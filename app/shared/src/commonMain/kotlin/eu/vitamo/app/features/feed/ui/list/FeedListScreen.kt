package eu.vitamo.app.features.feed.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.uuid.Uuid
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FeedListScreen(
    onCreateClicked: () -> Unit,
    onEditClicked: (String) -> Unit,
    onDetailClicked: (String) -> Unit,
    viewModel: FeedListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var pendingDeleteId by remember { mutableStateOf<Uuid?>(null) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Feed", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onCreateClicked) {
                Text("Nieuw")
            }
        }

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.items) { item ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(item.author.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(item.content.content.toString(), style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onDetailClicked(item.uuid.toString()) }) { Text("Open") }
                        Button(onClick = { onEditClicked(item.uuid.toString()) }) { Text("Bewerk") }
                        Button(onClick = { pendingDeleteId = item.uuid }) { Text("Verwijder") }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.loadPreviousPage() }) { Text("Vorige") }
            Button(onClick = { viewModel.loadNextPage() }, enabled = state.hasMore) { Text("Volgende") }
        }
    }

    val deleteId = pendingDeleteId
    if (deleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Item verwijderen?") },
            text = { Text("Dit item wordt soft-deleted en verdwijnt uit de feed.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(deleteId)
                        pendingDeleteId = null
                    },
                ) {
                    Text("Verwijder")
                }
            },
            dismissButton = {
                Button(onClick = { pendingDeleteId = null }) {
                    Text("Annuleer")
                }
            },
        )
    }
}
