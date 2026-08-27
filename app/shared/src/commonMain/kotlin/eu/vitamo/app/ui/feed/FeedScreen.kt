package eu.vitamo.app.ui.feed

import androidx.compose.foundation.lazy.rememberLazyListState
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
import eu.vitamo.app.ui.media.model.rememberFeedImagePickerLauncher
import io.ktor.client.HttpClient
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalCoilApi::class)
@Composable
fun FeedScreen(
    onCreatePostClicked: () -> Unit,
    viewModel: FeedViewModel =
        koinViewModel(),
) {
    val state by
        viewModel.state.collectAsState()

    val listState =
        rememberLazyListState()

    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    val httpClient =
        koinInject<HttpClient>()

    val mediaUrlResolver =
        koinInject<MediaUrlResolver>()

    val platformContext =
        LocalPlatformContext.current

    val imageLoader =
        remember(
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

    var showImageSourceDialog by remember {
        mutableStateOf(false)
    }

    val imagePicker =
        rememberFeedImagePickerLauncher(
            onImagePicked =
                viewModel::onImagePicked,
            onError =
                viewModel::onImagePickerError,
        )

    LaunchedEffect(
        viewModel,
        snackbarHostState,
    ) {
        viewModel.effects.collect { effect ->
            when (effect) {
                FeedEffect.OpenCreatePost -> {
                    onCreatePostClicked()
                }

                is FeedEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                    )
                }
            }
        }
    }

    FeedContent(
        state = state,
        listState = listState,
        snackbarHostState =
            snackbarHostState,
        imageLoader = imageLoader,
        mediaUrlResolver =
            mediaUrlResolver,
        onEvent =
            viewModel::onEvent,
        onCreatePostClicked = {
            showImageSourceDialog = true
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