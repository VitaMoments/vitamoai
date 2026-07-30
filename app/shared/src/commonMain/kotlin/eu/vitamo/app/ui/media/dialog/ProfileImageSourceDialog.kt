package eu.vitamo.app.ui.media.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ProfileImageSourceDialog(
    onDismissRequest: () -> Unit,
    onCameraClicked: () -> Unit,
    onGalleryClicked: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Profielfoto wijzigen",
            )
        },
        text = {
            Text(
                text = "Maak een nieuwe foto of kies een bestaande afbeelding.",
            )
        },
        confirmButton = {
            TextButton(
                onClick = onCameraClicked,
            ) {
                Text(
                    text = "Foto maken",
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onGalleryClicked,
            ) {
                Text(
                    text = "Kies uit galerij",
                )
            }
        },
    )
}