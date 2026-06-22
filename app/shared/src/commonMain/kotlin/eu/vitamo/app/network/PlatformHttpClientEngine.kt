package eu.vitamo.app.network

import io.ktor.client.engine.HttpClientEngineFactory

internal expect fun platformHttpClientEngineFactory(): HttpClientEngineFactory<*>

