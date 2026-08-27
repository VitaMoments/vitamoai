@file:OptIn(ExperimentalForeignApi::class)

package eu.vitamo.app.ui.media.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import eu.vitamo.app.features.media.model.PickedImage
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSUUID
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberProfileImagePickerLauncher(
    onImagePicked: (PickedImage) -> Unit,
    onError: (String) -> Unit,
): ProfileImagePickerLauncher =
    rememberImagePickerLauncher(
        gallerySelectionLimit = 1,
        onImagePicked = onImagePicked,
        onError = onError,
    )

@Composable
actual fun rememberFeedImagePickerLauncher(
    onImagePicked: (PickedImage) -> Unit,
    onError: (String) -> Unit,
): ProfileImagePickerLauncher =
    rememberImagePickerLauncher(
        gallerySelectionLimit = MAX_FEED_IMAGES,
        onImagePicked = onImagePicked,
        onError = onError,
    )

@Composable
private fun rememberImagePickerLauncher(
    gallerySelectionLimit: Long,
    onImagePicked: (PickedImage) -> Unit,
    onError: (String) -> Unit,
): ProfileImagePickerLauncher {
    val presentingViewController =
        LocalUIViewController.current

    val currentOnImagePicked =
        rememberUpdatedState(onImagePicked)

    val currentOnError =
        rememberUpdatedState(onError)

    val cameraDelegate = remember {
        CameraPickerDelegate(
            onImagePicked = { image ->
                currentOnImagePicked.value(image)
            },
            onError = { message ->
                currentOnError.value(message)
            },
        )
    }

    val galleryDelegate = remember {
        GalleryPickerDelegate(
            onImagePicked = { image ->
                currentOnImagePicked.value(image)
            },
            onError = { message ->
                currentOnError.value(message)
            },
        )
    }

    return remember(
        presentingViewController,
        cameraDelegate,
        galleryDelegate,
    ) {
        object : ProfileImagePickerLauncher {

            override fun launchCamera() {
                if (
                    !UIImagePickerController
                        .isSourceTypeAvailable(
                            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
                        )
                ) {
                    currentOnError.value(
                        "Op dit apparaat is geen camera beschikbaar.",
                    )

                    return
                }

                val cameraController =
                    UIImagePickerController().apply {
                        sourceType =
                            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera

                        allowsEditing = false
                        delegate = cameraDelegate
                    }

                presentingViewController.presentViewController(
                    viewControllerToPresent =
                        cameraController,
                    animated = true,
                    completion = null,
                )
            }

            override fun launchGallery() {
                val configuration =
                    PHPickerConfiguration().apply {
                        selectionLimit = 1
                        filter = PHPickerFilter.imagesFilter
                    }

                val galleryController =
                    PHPickerViewController(
                        configuration = configuration,
                    ).apply {
                        delegate = galleryDelegate
                    }

                presentingViewController.presentViewController(
                    viewControllerToPresent =
                        galleryController,
                    animated = true,
                    completion = null,
                )
            }
        }
    }
}

private class CameraPickerDelegate(
    private val onImagePicked: (PickedImage) -> Unit,
    private val onError: (String) -> Unit,
) : NSObject(),
    UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[
            UIImagePickerControllerOriginalImage
        ] as? UIImage

        picker.dismissViewControllerAnimated(
            flag = true,
            completion = null,
        )

        if (image == null) {
            onError(
                "De gemaakte foto kon niet worden verwerkt.",
            )

            return
        }

        val jpegData = UIImageJPEGRepresentation(
            image = image,
            compressionQuality = JPEG_QUALITY,
        )

        if (jpegData == null) {
            onError(
                "De gemaakte foto kon niet als JPEG worden opgeslagen.",
            )

            return
        }

        onImagePicked(
            jpegData.toPickedImage(
                prefix = CAMERA_FILE_PREFIX,
            ),
        )
    }

    override fun imagePickerControllerDidCancel(
        picker: UIImagePickerController,
    ) {
        picker.dismissViewControllerAnimated(
            flag = true,
            completion = null,
        )
    }
}

private class GalleryPickerDelegate(
    private val onImagePicked: (PickedImage) -> Unit,
    private val onError: (String) -> Unit,
) : NSObject(),
    PHPickerViewControllerDelegateProtocol {

    override fun picker(
        picker: PHPickerViewController,
        didFinishPicking: List<*>,
    ) {
        picker.dismissViewControllerAnimated(
            flag = true,
            completion = null,
        )

        val results =
            didFinishPicking
                .mapNotNull { result ->
                    result as? PHPickerResult
                }

        if (results.isEmpty()) {
            return
        }

        val pickedImages =
            MutableList<PickedImage?>(
                size = results.size,
            ) {
                null
            }

        var completedCount = 0

        results.forEachIndexed {
                index,
                result,
            ->

            val itemProvider =
                result.itemProvider

            if (
                !itemProvider
                    .hasItemConformingToTypeIdentifier(
                        UTTypeImage.identifier,
                    )
            ) {
                dispatch_async(
                    dispatch_get_main_queue(),
                ) {
                    onError(
                        "Het geselecteerde bestand is geen afbeelding.",
                    )

                    completedCount++

                    if (
                        completedCount ==
                        results.size
                    ) {
                        pickedImages
                            .filterNotNull()
                            .forEach(
                                onImagePicked,
                            )
                    }
                }

                return@forEachIndexed
            }

            itemProvider
                .loadDataRepresentationForTypeIdentifier(
                    typeIdentifier =
                        UTTypeImage.identifier,
                ) { data: NSData?, error: NSError? ->

                    dispatch_async(
                        dispatch_get_main_queue(),
                    ) {
                        when {
                            error != null -> {
                                onError(
                                    error.localizedDescription
                                        .takeIf(
                                            String::isNotBlank,
                                        )
                                        ?: "De afbeelding kon niet worden geladen.",
                                )
                            }

                            data == null -> {
                                onError(
                                    "De afbeelding bevat geen leesbare gegevens.",
                                )
                            }

                            else -> {
                                val image =
                                    UIImage(
                                        data = data,
                                    )

                                val jpegData =
                                    image?.let {
                                        UIImageJPEGRepresentation(
                                            image = it,
                                            compressionQuality =
                                                JPEG_QUALITY,
                                        )
                                    }

                                if (jpegData == null) {
                                    onError(
                                        "De afbeelding kon niet naar JPEG worden omgezet.",
                                    )
                                } else {
                                    pickedImages[index] =
                                        jpegData.toPickedImage(
                                            prefix =
                                                GALLERY_FILE_PREFIX,
                                        )
                                }
                            }
                        }

                        completedCount++

                        if (
                            completedCount ==
                            results.size
                        ) {
                            /*
                             * Eerst alles laden en daarna in dezelfde
                             * volgorde teruggeven als de selectie.
                             */
                            pickedImages
                                .filterNotNull()
                                .forEach(
                                    onImagePicked,
                                )
                        }
                    }
                }
        }
    }
}

private fun NSData.toPickedImage(
    prefix: String,
): PickedImage {
    val fileName =
        "$prefix${NSUUID().UUIDString}.jpg"

    return PickedImage(
        data = this,
        fileName = fileName,
        mimeType = JPEG_MIME_TYPE,
    )
}

private const val MAX_FEED_IMAGES =
    5L

private const val JPEG_QUALITY =
    0.9

private const val JPEG_MIME_TYPE =
    "image/jpeg"

private const val CAMERA_FILE_PREFIX =
    "camera-profile-image-"

private const val GALLERY_FILE_PREFIX =
    "gallery-profile-image-"