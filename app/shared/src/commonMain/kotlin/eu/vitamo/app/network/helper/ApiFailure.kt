package eu.vitamo.app.network.helper

import eu.vitamo.app.api.result.ApiError
import eu.vitamo.app.api.result.ApiFailure

fun ApiFailure.serverErrorOrNull(): ApiError? =
    (this as? ApiFailure.Server)?.apiError

fun ApiFailure.hasHttpStatus(status: Int): Boolean =
    serverErrorOrNull()?.status == status

fun ApiFailure.isUnauthorized(): Boolean =
    hasHttpStatus(status = 401)