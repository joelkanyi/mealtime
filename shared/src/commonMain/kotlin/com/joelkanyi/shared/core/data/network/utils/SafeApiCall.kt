/*
 * Copyright 2023 Joel Kanyi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.joelkanyi.shared.core.data.network.utils

import com.joelkanyi.shared.core.data.network.model.ErrorResponse
import io.github.aakira.napier.Napier
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.json.Json

suspend fun <T : Any> safeApiCall(apiCall: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(data = apiCall.invoke())
    } catch (e: RedirectResponseException) { // 3xx errors
        val networkError = parseNetworkError(
            errorResponse = e.response,
            exception = e,
        )
        Resource.Error(
            message = networkError.message ?: networkError.errors.firstOrNull()
            ?: e.message,
        )
    } catch (e: ClientRequestException) { // 4xx errors
        val networkError = parseNetworkError(
            errorResponse = e.response,
            exception = e,
        )

        Resource.Error(
            message = networkError.message ?: networkError.message
            ?: e.message,
        )
    } catch (e: ServerResponseException) { // 5xx errors
        val networkError = parseNetworkError(
            errorResponse = e.response,
            exception = e,
        )
        Resource.Error(
            message = networkError.message ?: networkError.errors.firstOrNull()
            ?: e.message,
        )
    } catch (e: UnresolvedAddressException) {
        val networkError = parseNetworkError(
            exception = e,
        )
        Resource.Error(
            message = networkError.message ?: networkError.message
            ?: e.message,
        )
    } catch (e: Exception) {
        Resource.Error(
            message = "An unknown error occurred",
        )
    }
}

internal suspend fun parseNetworkError(
    errorResponse: HttpResponse? = null,
    exception: Exception? = null,
): ErrorResponse {
    return errorResponse?.bodyAsText()?.let {
        Napier.e("Error response: $it")
        Json.decodeFromString(ErrorResponse.serializer(), it)
    } ?: ErrorResponse(
        message = exception?.message ?: "An unknown error occurred",
    )
}
