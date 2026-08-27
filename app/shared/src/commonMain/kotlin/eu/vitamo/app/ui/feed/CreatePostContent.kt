package eu.vitamo.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.vitamo.app.features.media.model.PickedImage
import eu.vitamo.app.ui.theme.VitaDimensions
@Composable
fun CreatePostContent(
    state: CreatePostState,
    snackbarHostState: SnackbarHostState,
    onEvent: (CreatePostEvent) -> Unit,
    onAddImagesClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions =
        VitaDimensions.current

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(
                hostState =
                    snackbarHostState,
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nieuw bericht",
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = {
                            onEvent(
                                CreatePostEvent.Cancel,
                            )
                        },
                        enabled =
                            !state.isSubmitting,
                    ) {
                        Text(
                            text = "Annuleren",
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onEvent(
                                CreatePostEvent.Submit,
                            )
                        },
                        enabled =
                            state.canSubmit,
                    ) {
                        Text(
                            text =
                                if (
                                    state.isSubmitting
                                ) {
                                    "Plaatsen…"
                                } else {
                                    "Plaatsen"
                                },
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            rememberScrollState(),
                        )
                        .padding(
                            dimensions.screenPadding,
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        dimensions.lg,
                    ),
            ) {
                OutlinedTextField(
                    value =
                        state.text,
                    onValueChange = { text ->
                        onEvent(
                            CreatePostEvent.TextChanged(
                                text = text,
                            ),
                        )
                    },
                    label = {
                        Text(
                            text = "Tekst",
                        )
                    },
                    placeholder = {
                        Text(
                            text =
                                "Schrijf iets bij je foto's…",
                        )
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    minLines = 5,
                    maxLines = 10,
                    enabled =
                        !state.isSubmitting,
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Foto's",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold,
                    )

                    Text(
                        text =
                            "${state.images.size}/${CreatePostState.MAX_IMAGES}",
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                    )
                }

                SelectedImages(
                    images =
                        state.images,
                    enabled =
                        !state.isSubmitting,
                    onRemoveImage = { index ->
                        onEvent(
                            CreatePostEvent.RemoveImage(
                                index = index,
                            ),
                        )
                    },
                )

                if (state.canAddImages) {
                    OutlinedButton(
                        onClick =
                            onAddImagesClicked,
                        enabled =
                            !state.isSubmitting,
                        modifier =
                            Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text =
                                "Foto toevoegen",
                        )
                    }
                }

                Button(
                    onClick = {
                        onEvent(
                            CreatePostEvent.Submit,
                        )
                    },
                    enabled =
                        state.canSubmit,
                    modifier =
                        Modifier.fillMaxWidth(),
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    dimensions.xl,
                                ),
                            strokeWidth = 2.dp,
                        )

                        Spacer(
                            modifier =
                                Modifier.size(
                                    dimensions.sm,
                                ),
                        )

                        Text(
                            text = "Plaatsen…",
                        )
                    } else {
                        Text(
                            text = "Plaatsen",
                        )
                    }
                }
            }

            if (state.isSubmitting) {
                LinearProgressIndicator(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(
                                Alignment.TopCenter,
                            ),
                )
            }
        }
    }
}

@Composable
private fun SelectedImages(
    images: List<PickedImage>,
    enabled: Boolean,
    onRemoveImage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions =
        VitaDimensions.current

    Column(
        modifier = modifier,
        verticalArrangement =
            Arrangement.spacedBy(
                dimensions.sm,
            ),
    ) {
        images.forEachIndexed {
                index,
                image,
            ->

            SelectedImageItem(
                image = image,
                index = index,
                enabled = enabled,
                onRemove = {
                    onRemoveImage(index)
                },
            )
        }
    }
}

@Composable
private fun SelectedImageItem(
    image: PickedImage,
    index: Int,
    enabled: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions =
        VitaDimensions.current

    Card(
        modifier =
            modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        dimensions.cardPaddingLarge,
                    ),
            verticalAlignment =
                Alignment.CenterVertically,
        ) {
            Column(
                modifier =
                    Modifier.weight(1f),
            ) {
                Text(
                    text =
                        "Foto ${index + 1}",
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,
                )

                Text(
                    text = image.fileName,
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,
                    fontWeight =
                        FontWeight.Medium,
                )

                Text(
                    text = image.mimeType,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }

            TextButton(
                onClick = onRemove,
                enabled = enabled,
            ) {
                Text(
                    text = "Verwijderen",
                )
            }
        }
    }
}