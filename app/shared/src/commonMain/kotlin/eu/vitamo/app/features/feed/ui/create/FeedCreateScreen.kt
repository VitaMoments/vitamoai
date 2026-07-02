package eu.vitamo.app.features.feed.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FeedCreateScreen(
    onCreated: () -> Unit,
    viewModel: FeedCreateViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    if (state.created) {
        onCreated()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Nieuw feed item", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.content,
            onValueChange = viewModel::onContentChanged,
            label = { Text("Content") },
        )
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = viewModel::submit, enabled = !state.isLoading) {
            Text("Opslaan")
        }
    }
}
