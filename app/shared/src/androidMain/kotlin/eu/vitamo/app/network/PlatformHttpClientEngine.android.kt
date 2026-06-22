package eu.vitamo.app.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun platformHttpClientEngineFactory(): HttpClientEngineFactory<*> = OkHttp

