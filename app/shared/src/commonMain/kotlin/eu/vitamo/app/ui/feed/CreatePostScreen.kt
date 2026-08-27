package eu.vitamo.app.ui.feed

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import eu.vitamo.app.ui.media.dialog.ProfileImageSourceDialog
import eu.vitamo.app.ui.media.model.rememberFeedImagePickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreatePostScreen(
    onClose: () -> Unit,
    onPostCreated: () -> Unit,
    viewModel: CreatePostViewModel =
        koinViewModel(),
) {
    val state by
        viewModel.state.collectAsState()

    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    var showImageSourceDialog by remember {
        mutableStateOf(false)
    }

    val imagePicker =
        rememberFeedImagePickerLauncher(
            onImagePicked = { image ->
                viewModel.onEvent(
                    CreatePostEvent.ImagePicked(
                        image = image,
                    ),
                )
            },
            onError =
                viewModel::onImagePickerError,
        )

    LaunchedEffect(
        viewModel,
        snackbarHostState,
    ) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CreatePostEffect.PostCreated -> {
                    onPostCreated()
                }

                CreatePostEffect.Close -> {
                    onClose()
                }

                is CreatePostEffect.ShowMessage -> {
                    snackbarHostState
                        .showSnackbar(
                            message =
                                effect.message,
                        )
                }
            }
        }
    }

    CreatePostContent(
        state = state,
        snackbarHostState =
            snackbarHostState,
        onEvent =
            viewModel::onEvent,
        onAddImagesClicked = {
            if (
                state.canAddImages &&
                !state.isSubmitting
            ) {
                showImageSourceDialog =
                    true
            }
        },
    )

    if (showImageSourceDialog) {
        ProfileImageSourceDialog(
            onDismissRequest = {
                showImageSourceDialog =
                    false
            },
            onCameraClicked = {
                showImageSourceDialog =
                    false

                imagePicker.launchCamera()
            },
            onGalleryClicked = {
                showImageSourceDialog =
                    false

                imagePicker.launchGallery()
            },
        )
    }
}