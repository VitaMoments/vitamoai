package eu.vitamo.app.ui.media.model

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import eu.vitamo.app.features.media.model.PickedImage
import java.io.File

@Composable
actual fun rememberProfileImagePickerLauncher(
    onImagePicked: (PickedImage) -> Unit,
    onError: (String) -> Unit,
): ProfileImagePickerLauncher =
    rememberImagePickerLauncher(
        galleryMode = GalleryMode.SINGLE,
        onImagePicked = onImagePicked,
        onError = onError,
    )

@Composable
actual fun rememberFeedImagePickerLauncher(
    onImagePicked: (PickedImage) -> Unit,
    onError: (String) -> Unit,
): ProfileImagePickerLauncher =
    rememberImagePickerLauncher(
        galleryMode = GalleryMode.MULTIPLE,
        onImagePicked = onImagePicked,
        onError = onError,
    )

@Composable
private fun rememberImagePickerLauncher(
    galleryMode: GalleryMode,
    onImagePicked: (PickedImage) -> Unit,
    onError: (String) -> Unit,
): ProfileImagePickerLauncher {
    val context = LocalContext.current
    val applicationContext = context.applicationContext

    val currentOnImagePicked by rememberUpdatedState(
        newValue = onImagePicked,
    )

    val currentOnError by rememberUpdatedState(
        newValue = onError,
    )

    var pendingCameraFile by remember {
        mutableStateOf<File?>(null)
    }

    var pendingCameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val singleGalleryLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            createPickedImage(
                context = context,
                applicationContext = applicationContext,
                uri = uri,
                onImagePicked = currentOnImagePicked,
                onError = currentOnError,
            )
        }

    val multipleGalleryLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.PickMultipleVisualMedia(
                    maxItems = MAX_FEED_IMAGES,
                ),
        ) { uris ->
            uris.forEach { uri ->
                createPickedImage(
                    context = context,
                    applicationContext = applicationContext,
                    uri = uri,
                    onImagePicked = currentOnImagePicked,
                    onError = currentOnError,
                )
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.TakePicture(),
        ) { success ->
            val file = pendingCameraFile
            val uri = pendingCameraUri

            pendingCameraFile = null
            pendingCameraUri = null

            if (
                !success ||
                file == null ||
                uri == null ||
                !file.isFile ||
                file.length() <= 0L
            ) {
                file?.delete()

                if (success) {
                    currentOnError(
                        "De gemaakte foto kon niet worden geopend.",
                    )
                }

                return@rememberLauncherForActivityResult
            }

            currentOnImagePicked(
                PickedImage(
                    context = applicationContext,
                    uri = uri,
                    fileName = file.name,
                    mimeType = JPEG_MIME_TYPE,
                    temporaryFile = file,
                ),
            )
        }

    return remember(
        applicationContext,
        galleryMode,
        singleGalleryLauncher,
        multipleGalleryLauncher,
        cameraLauncher,
    ) {
        object : ProfileImagePickerLauncher {

            override fun launchCamera() {
                runCatching {
                    pendingCameraFile?.delete()

                    val directory =
                        File(
                            applicationContext.cacheDir,
                            PROFILE_IMAGE_CACHE_DIRECTORY,
                        ).apply {
                            check(
                                exists() || mkdirs(),
                            ) {
                                "De tijdelijke cameramap kon niet worden aangemaakt."
                            }
                        }

                    val temporaryFile =
                        File.createTempFile(
                            CAMERA_FILE_PREFIX,
                            CAMERA_FILE_SUFFIX,
                            directory,
                        )

                    val contentUri =
                        FileProvider.getUriForFile(
                            applicationContext,
                            "${applicationContext.packageName}.$FILE_PROVIDER_SUFFIX",
                            temporaryFile,
                        )

                    pendingCameraFile =
                        temporaryFile

                    pendingCameraUri =
                        contentUri

                    cameraLauncher.launch(
                        contentUri,
                    )
                }.onFailure { cause ->
                    pendingCameraFile?.delete()

                    pendingCameraFile = null
                    pendingCameraUri = null

                    currentOnError(
                        cause.message
                            ?: "De camera kon niet worden geopend.",
                    )
                }
            }

            override fun launchGallery() {
                runCatching {
                    val request =
                        PickVisualMediaRequest(
                            mediaType =
                                ActivityResultContracts
                                    .PickVisualMedia
                                    .ImageOnly,
                        )

                    when (galleryMode) {
                        GalleryMode.SINGLE -> {
                            singleGalleryLauncher.launch(
                                request,
                            )
                        }

                        GalleryMode.MULTIPLE -> {
                            multipleGalleryLauncher.launch(
                                request,
                            )
                        }
                    }
                }.onFailure { cause ->
                    currentOnError(
                        cause.message
                            ?: "De galerij kon niet worden geopend.",
                    )
                }
            }
        }
    }
}

private fun createPickedImage(
    context: Context,
    applicationContext: Context,
    uri: Uri,
    onImagePicked: (PickedImage) -> Unit,
    onError: (String) -> Unit,
) {
    runCatching {
        PickedImage(
            context = applicationContext,
            uri = uri,
            fileName =
                context.getDisplayName(
                    uri = uri,
                ),
            mimeType =
                context.contentResolver
                    .getType(uri)
                    ?: DEFAULT_IMAGE_MIME_TYPE,
            temporaryFile = null,
        )
    }.onSuccess(
        action = onImagePicked,
    ).onFailure { cause ->
        onError(
            cause.message
                ?: "De afbeelding kon niet worden geselecteerd.",
        )
    }
}

private fun Context.getDisplayName(
    uri: Uri,
): String {
    contentResolver.query(
        uri,
        arrayOf(
            OpenableColumns.DISPLAY_NAME,
        ),
        null,
        null,
        null,
    )?.use { cursor ->
        val columnIndex =
            cursor.getColumnIndex(
                OpenableColumns.DISPLAY_NAME,
            )

        if (
            columnIndex >= 0 &&
            cursor.moveToFirst()
        ) {
            return cursor
                .getString(columnIndex)
                ?.takeIf(String::isNotBlank)
                ?: DEFAULT_GALLERY_FILE_NAME
        }
    }

    return uri.lastPathSegment
        ?.substringAfterLast('/')
        ?.takeIf(String::isNotBlank)
        ?: DEFAULT_GALLERY_FILE_NAME
}

private enum class GalleryMode {
    SINGLE,
    MULTIPLE,
}

private const val MAX_FEED_IMAGES = 5

private const val PROFILE_IMAGE_CACHE_DIRECTORY =
    "profile-images"

private const val CAMERA_FILE_PREFIX =
    "profile-image-"

private const val CAMERA_FILE_SUFFIX =
    ".jpg"

private const val JPEG_MIME_TYPE =
    "image/jpeg"

private const val DEFAULT_IMAGE_MIME_TYPE =
    "application/octet-stream"

private const val DEFAULT_GALLERY_FILE_NAME =
    "profile-image"

private const val FILE_PROVIDER_SUFFIX =
    "profile-image-file-provider"