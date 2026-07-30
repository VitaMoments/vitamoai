package eu.vitamo.app.ui.user.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import eu.vitamo.app.features.media.model.MediaUrlResolver
import eu.vitamo.app.ui.media.dialog.ProfileImageSourceDialog
import eu.vitamo.app.ui.media.model.rememberProfileImagePickerLauncher
import io.ktor.client.HttpClient
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ProfileScreen(
    onBackClicked: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val httpClient = koinInject<HttpClient>()
    val mediaUrlResolver = koinInject<MediaUrlResolver>()

    val platformContext = LocalPlatformContext.current

    val imageLoader = remember(
        platformContext,
        httpClient,
    ) {
        ImageLoader.Builder(
            context = platformContext,
        )
            .components {
                add(
                    KtorNetworkFetcherFactory(
                        httpClient = httpClient,
                    ),
                )
            }
            .crossfade(true)
            .build()
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    var showImageSourceDialog by remember {
        mutableStateOf(false)
    }

    val imagePicker = rememberProfileImagePickerLauncher(
        onImagePicked = viewModel::updateProfileImage,
        onError = viewModel::onImagePickerError,
    )

    LaunchedEffect(viewModel) {
        viewModel.loadProfile()
    }

    LaunchedEffect(
        viewModel,
        snackbarHostState,
    ) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                    )
                }
            }
        }
    }

    ProfileContent(
        state = state,
        profileImageUrl = mediaUrlResolver.resolve(
            media = state.profile
                ?.user
                ?.profileImage,
        ),
        imageLoader = imageLoader,
        onRetryClicked = viewModel::retry,
        onChangeProfileImageClicked = {
            if (!state.isProfileImageUploading) {
                showImageSourceDialog = true
            }
        },
    )

    if (showImageSourceDialog) {
        ProfileImageSourceDialog(
            onDismissRequest = {
                showImageSourceDialog = false
            },
            onCameraClicked = {
                showImageSourceDialog = false
                imagePicker.launchCamera()
            },
            onGalleryClicked = {
                showImageSourceDialog = false
                imagePicker.launchGallery()
            },
        )
    }
}