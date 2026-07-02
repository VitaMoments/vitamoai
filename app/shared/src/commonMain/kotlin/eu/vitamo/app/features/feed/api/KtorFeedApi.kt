package eu.vitamo.app.features.feed.api

import eu.vitamo.app.api.contracts.feed.CreateFeedItemRequest
import eu.vitamo.app.api.contracts.feed.FeedItem
import eu.vitamo.app.api.contracts.feed.FeedItemsPageResponse
import eu.vitamo.app.api.contracts.feed.UpdateFeedItemRequest
import eu.vitamo.app.api.result.ApiResult
import eu.vitamo.app.network.auth.AuthSessionCoordinator
import eu.vitamo.app.network.helper.safeAuthenticatedApiCall
import eu.vitamo.app.network.helper.safeAuthenticatedUnitCall
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.uuid.Uuid

class KtorFeedApi(
    private val client: HttpClient,
    private val config: FeedApiConfig,
    private val authSessionCoordinator: AuthSessionCoordinator,
) : FeedApi {
    override suspend fun createFeedItem(request: CreateFeedItemRequest): ApiResult<FeedItem> {
        return safeAuthenticatedApiCall(authSessionCoordinator) {
            client.post(config.baseUrl) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun getFeedItem(uuid: Uuid): ApiResult<FeedItem> {
        return safeAuthenticatedApiCall(authSessionCoordinator) {
            client.get("${config.baseUrl}/$uuid")
        }
    }

    override suspend fun getMyFeed(limit: Int, offset: Int): ApiResult<FeedItemsPageResponse> {
        return safeAuthenticatedApiCall(authSessionCoordinator) {
            client.get("${config.baseUrl}/me?limit=$limit&offset=$offset")
        }
    }

    override suspend fun getGeneralFeed(limit: Int, offset: Int, categories: Set<String>): ApiResult<FeedItemsPageResponse> {
        val categoryQuery = categories.takeIf { it.isNotEmpty() }?.joinToString(",")
        val query = buildString {
            append("?limit=$limit&offset=$offset")
            if (categoryQuery != null) {
                append("&categories=$categoryQuery")
            }
        }
        return safeAuthenticatedApiCall(authSessionCoordinator) {
            client.get(config.baseUrl + query)
        }
    }

    override suspend fun updateFeedItem(uuid: Uuid, request: UpdateFeedItemRequest): ApiResult<FeedItem> {
        return safeAuthenticatedApiCall(authSessionCoordinator) {
            client.patch("${config.baseUrl}/$uuid") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun deleteFeedItem(uuid: Uuid): ApiResult<Unit> {
        return safeAuthenticatedUnitCall(authSessionCoordinator) {
            client.delete("${config.baseUrl}/$uuid")
        }
    }
}
