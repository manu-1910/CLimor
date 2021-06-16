package com.limor.app.apollo

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import com.limor.app.apollo.interceptors.AuthInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.IOException

const val GRAPHQL_ENDPOINT = "https://apigateway.dev.limor.ie/graphql"

object Apollo {

    private val client = ApolloClient.builder()
        .serverUrl(GRAPHQL_ENDPOINT)
        .okHttpClient(
            OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor())
                .build()
        )
        .build()

    suspend fun <A : Operation.Data, B, C : Operation.Variables> launchQuery(query: Query<A, B, C>): Response<B>? =
        withContext(Dispatchers.IO) {
            val result = client.query(query).await()
            checkResultForErrors(result)
            result
        }

    suspend fun <A : Operation.Data, B, C : Operation.Variables> mutate(query: Mutation<A, B, C>): Response<B>? =
        withContext(Dispatchers.IO) {
            val result = client.mutate(query).await()
            checkResultForErrors(result)
            result
        }

    private fun <T> checkResultForErrors(response: Response<T>) {
        if (response.hasErrors()) {
            val apiException = GraphqlClientException(response.errors!![0].message)
            Timber.e(apiException)
            throw apiException
        }
    }
}

class GraphqlClientException(message: String) : Exception(message)

fun showHumanizedErrorMessage(e: Exception): String {
    if (e is IOException || e.cause is IOException)
        return "Check internet"
    return e.message ?: "Network client error"
}