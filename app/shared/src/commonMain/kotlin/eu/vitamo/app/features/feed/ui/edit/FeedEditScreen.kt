package eu.vitamo.app.features.feed.ui.edit

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
import kotlin.uuid.Uuid
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FeedEditScreen(
    uuid: String,
    onSaved: () -> Unit,
    viewModel: FeedEditViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    if (state.saved) {
        onSaved()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Feed item bewerken", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.content,
            onValueChange = viewModel::onContentChanged,
            label = { Text("Content") },
        )
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { viewModel.save(Uuid.parse(uuid)) }, enabled = !state.isLoading) {
            Text("Opslaan")
        }
    }
}
