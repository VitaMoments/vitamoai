package eu.vitamo.app.ui.media.model

import androidx.compose.runtime.Composable
import eu.vitamo.app.features.media.model.PickedImage

interface ProfileImagePickerLauncher {

    fun launchCamera()

    fun launchGallery()
}

@Composable
expect fun rememberProfileImagePickerLauncher(
    onImagePicked: (PickedImage) -> Unit,
    onError: (String) -> Unit,
): ProfileImagePickerLauncher

@Composable
expect fun rememberFeedImagePickerLauncher(
    onImagePicked: (PickedImage) -> Unit,
    onError: (String) -> Unit,
): ProfileImagePickerLauncher