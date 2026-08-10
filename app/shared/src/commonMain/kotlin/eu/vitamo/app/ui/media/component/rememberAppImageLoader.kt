@file:OptIn(ExperimentalCoilApi::class)

package eu.vitamo.app.ui.media.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import io.ktor.client.HttpClient
import org.koin.compose.koinInject

@Composable
fun rememberAppImageLoader(): ImageLoader {
    val httpClient: HttpClient = koinInject()
    val platformContext = LocalPlatformContext.current

    return remember(
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
}